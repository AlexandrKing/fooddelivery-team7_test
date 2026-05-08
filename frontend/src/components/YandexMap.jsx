import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { Map, Placemark, YMaps } from '@pbe/react-yandex-maps';

const MOSCOW_CENTER = [55.751574, 37.573856];
const DEFAULT_CENTER = [55.7558, 37.6173];
const DEFAULT_ZOOM = 10;
const RESTAURANT_MARKER_ICON = `data:image/svg+xml;charset=UTF-8,${encodeURIComponent(`
<svg xmlns="http://www.w3.org/2000/svg" width="40" height="52" viewBox="0 0 40 52">
  <path d="M20 51C16 43 4 31 4 19a16 16 0 1 1 32 0c0 12-12 24-16 32Z" fill="#e11d48"/>
  <circle cx="20" cy="19" r="7" fill="#fff"/>
  <circle cx="20" cy="19" r="3.5" fill="#e11d48"/>
</svg>
`)}`;
const USER_MARKER_ICON = `data:image/svg+xml;charset=UTF-8,${encodeURIComponent(`
<svg xmlns="http://www.w3.org/2000/svg" width="42" height="42" viewBox="0 0 42 42">
  <circle cx="21" cy="21" r="20" fill="#2563eb" fill-opacity="0.18"/>
  <circle cx="21" cy="21" r="11" fill="#2563eb" stroke="#fff" stroke-width="4"/>
  <circle cx="21" cy="21" r="4" fill="#bfdbfe"/>
</svg>
`)}`;

function escapeHtml(value) {
  return String(value)
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#39;');
}

function toPoint(restaurant) {
  const lat = Number(restaurant?.lat ?? restaurant?.latitude);
  const lng = Number(restaurant?.lng ?? restaurant?.longitude);
  if (!Number.isFinite(lat) || !Number.isFinite(lng)) {
    return null;
  }
  return {
    id: restaurant?.id ?? `${restaurant?.name}-${lat}-${lng}`,
    domId: encodeURIComponent(String(restaurant?.id ?? `${restaurant?.name}-${lat}-${lng}`)),
    restaurantId: restaurant?.id,
    restaurant,
    name: restaurant?.name || 'Ресторан',
    address: restaurant?.address || 'Адрес не указан',
    cuisine: restaurant?.cuisineType ?? restaurant?.cuisine ?? '',
    deliveryTime: restaurant?.deliveryTime ?? null,
    geometry: [lat, lng],
  };
}

function buildBalloonContentBody(point) {
  const rows = [
    point.address ? `<p><strong>Адрес:</strong> ${escapeHtml(point.address)}</p>` : '',
    point.cuisine ? `<p><strong>Кухня:</strong> ${escapeHtml(point.cuisine)}</p>` : '',
    point.deliveryTime != null
      ? `<p><strong>Доставка:</strong> до ${escapeHtml(point.deliveryTime)} мин</p>`
      : '',
  ].filter(Boolean);

  return `<div class="map-balloon">${rows.join('')}</div>`;
}

function getLocationErrorMessage(error) {
  switch (error?.code) {
    case 1:
      return 'Доступ к геолокации запрещен в браузере.';
    case 2:
      return 'Не удалось определить местоположение.';
    case 3:
      return 'Геолокация не ответила вовремя.';
    default:
      return 'Браузер не смог определить местоположение.';
  }
}

export default function YandexMap({ restaurants = [], onSelectRestaurant }) {
  const apiKey = (import.meta.env.VITE_YANDEX_MAPS_API_KEY ?? '').trim();
  const mapRef = useRef(null);
  const placemarkSetRef = useRef(new WeakSet());
  const [userLocation, setUserLocation] = useState(null);
  const [locationStatus, setLocationStatus] = useState('idle');
  const [locationMessage, setLocationMessage] = useState('');
  const points = useMemo(() => restaurants.map(toPoint).filter(Boolean), [restaurants]);
  const mapGeometries = useMemo(() => points.map((point) => point.geometry), [points]);

  const bindBalloonButton = useCallback(
    (point) => {
      const mapContainer = mapRef.current?.container?.getElement?.();
      if (!mapContainer) {
        return;
      }
      const button = mapContainer.querySelector(`[data-restaurant-id="${point.domId}"]`);
      if (!button || button.dataset.bound === '1') {
        return;
      }
      button.dataset.bound = '1';
      button.addEventListener('click', (event) => {
        event.preventDefault();
        if (typeof onSelectRestaurant === 'function') {
          onSelectRestaurant(point.restaurant);
        }
      });
    },
    [onSelectRestaurant]
  );

  const bindPlacemarkEvents = useCallback(
    (placemarkInstance, point) => {
      if (!placemarkInstance || placemarkSetRef.current.has(placemarkInstance)) {
        return;
      }
      placemarkSetRef.current.add(placemarkInstance);
      placemarkInstance.events.add('balloonopen', () => {
        bindBalloonButton(point);
        const map = mapRef.current;
        if (map?.panTo) {
          map.panTo(point.geometry, {
            duration: 250,
            flying: true,
            checkZoomRange: true,
          });
        }
      });
    },
    [bindBalloonButton]
  );

  const focusUserLocation = useCallback((geometry) => {
    const map = mapRef.current;
    if (map?.setCenter) {
      map.setCenter(geometry, 14, {
        duration: 250,
        checkZoomRange: true,
      });
    }
  }, []);

  const requestUserLocation = useCallback(() => {
    if (typeof navigator === 'undefined' || !navigator.geolocation) {
      setLocationStatus('error');
      setLocationMessage('Браузер не поддерживает геолокацию.');
      return;
    }

    if (userLocation) {
      focusUserLocation(userLocation.geometry);
    }

    setLocationStatus('loading');
    setLocationMessage('');

    navigator.geolocation.getCurrentPosition(
      (position) => {
        const geometry = [position.coords.latitude, position.coords.longitude];
        if (geometry.every(Number.isFinite)) {
          setUserLocation({
            accuracy: Number.isFinite(position.coords.accuracy) ? position.coords.accuracy : null,
            geometry,
          });
          setLocationStatus('success');
          focusUserLocation(geometry);
          return;
        }
        setLocationStatus('error');
        setLocationMessage('Браузер вернул некорректные координаты.');
      },
      (error) => {
        setLocationStatus('error');
        setLocationMessage(getLocationErrorMessage(error));
      },
      {
        enableHighAccuracy: true,
        maximumAge: 60000,
        timeout: 10000,
      }
    );
  }, [focusUserLocation, userLocation]);

  useEffect(() => {
    const map = mapRef.current;
    if (!map) {
      return;
    }
    if (mapGeometries.length === 0) {
      map.setCenter(DEFAULT_CENTER, DEFAULT_ZOOM, { duration: 250 });
      return;
    }
    if (mapGeometries.length === 1) {
      map.setCenter(mapGeometries[0], DEFAULT_ZOOM, { duration: 250 });
      return;
    }

    const lats = mapGeometries.map((geometry) => geometry[0]);
    const lngs = mapGeometries.map((geometry) => geometry[1]);
    const bounds = [
      [Math.min(...lats), Math.min(...lngs)],
      [Math.max(...lats), Math.max(...lngs)],
    ];
    map.setBounds(bounds, {
      checkZoomRange: true,
      zoomMargin: 40,
      duration: 250,
    });
  }, [mapGeometries]);

  if (!apiKey) {
    return (
      <section className="yandex-map-section">
        <h3 className="yandex-map-section__title">Наше расположение</h3>
        <div
          className="map-wrapper map-wrapper--unavailable"
          role="alert"
          style={{ width: '100%', height: '450px', position: 'relative' }}
        >
          <p className="state state--error">
            Не задан VITE_YANDEX_MAPS_API_KEY. Добавьте ключ Яндекс Карт в env-файл frontend.
          </p>
        </div>
      </section>
    );
  }

  return (
    <section className="yandex-map-section">
      <h3 className="yandex-map-section__title">Наше расположение</h3>
      <div
        className="map-wrapper"
        style={{ width: '100%', height: '450px', position: 'relative' }}
      >
        <YMaps query={{ apikey: apiKey, lang: 'ru_RU' }}>
          <Map
            defaultState={{ center: MOSCOW_CENTER, zoom: 10 }}
            width="100%"
            height="100%"
            instanceRef={mapRef}
          >
            {points.map((point) => (
              <Placemark
                key={point.id}
                geometry={point.geometry}
                modules={['geoObject.addon.balloon', 'geoObject.addon.hint']}
                properties={{
                  hintContent: point.name,
                  balloonContentHeader: point.name,
                  balloonContentBody: buildBalloonContentBody(point),
                  balloonContentFooter:
                    '<button type="button" '
                    + `data-restaurant-id="${point.domId}" `
                    + 'style="font-size: 12px; padding: 6px 10px; border: 1px solid #d1d5db; border-radius: 8px; background: #fff; cursor: pointer;">'
                    + 'Перейти в ресторан'
                    + '</button>',
                }}
                options={{
                  balloonAutoPan: true,
                  hideIconOnBalloonOpen: false,
                  iconImageHref: RESTAURANT_MARKER_ICON,
                  iconImageOffset: [-20, -52],
                  iconImageSize: [40, 52],
                  iconLayout: 'default#image',
                  zIndex: 2000,
                }}
                instanceRef={(placemarkInstance) => bindPlacemarkEvents(placemarkInstance, point)}
              />
            ))}
            {userLocation && (
              <Placemark
                geometry={userLocation.geometry}
                modules={['geoObject.addon.balloon', 'geoObject.addon.hint']}
                properties={{
                  hintContent: 'Вы здесь',
                  balloonContentHeader: 'Ваше местоположение',
                  balloonContentBody:
                    '<p>Текущая позиция, определенная браузером.</p>'
                    + (userLocation.accuracy != null
                      ? `<p>Точность: до ${Math.round(userLocation.accuracy)} м</p>`
                      : ''),
                }}
                options={{
                  balloonAutoPan: true,
                  hideIconOnBalloonOpen: false,
                  iconImageHref: USER_MARKER_ICON,
                  iconImageOffset: [-21, -21],
                  iconImageSize: [42, 42],
                  iconLayout: 'default#image',
                  zIndex: 3000,
                }}
              />
            )}
          </Map>
        </YMaps>
        <div className="map-location-control">
          <button
            type="button"
            className="map-location-control__button"
            onClick={requestUserLocation}
            disabled={locationStatus === 'loading'}
          >
            {locationStatus === 'loading'
              ? 'Определяем...'
              : userLocation
                ? 'Показать меня'
                : 'Мое местоположение'}
          </button>
          {locationMessage && (
            <p
              className="map-location-control__message map-location-control__message--error"
              role="alert"
            >
              {locationMessage}
            </p>
          )}
        </div>
      </div>
    </section>
  );
}
