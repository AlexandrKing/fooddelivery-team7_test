import { useCallback, useEffect, useMemo, useRef } from 'react';
import { Map, Placemark, YMaps } from 'react-yandex-maps';

const DEFAULT_CENTER = [55.7558, 37.6173];
const DEFAULT_ZOOM = 10;

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
    description: restaurant?.cuisineType || 'Выберите ресторан из списка',
    geometry: [lat, lng],
  };
}

export default function YandexMap({ restaurants = [], onSelectRestaurant }) {
  const apiKey = import.meta.env.VITE_YANDEX_MAPS_API_KEY;
  const mapRef = useRef(null);
  const placemarkSetRef = useRef(new WeakSet());
  const points = useMemo(() => restaurants.map(toPoint).filter(Boolean), [restaurants]);
  const center = points[0]?.geometry ?? DEFAULT_CENTER;

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

  useEffect(() => {
    const map = mapRef.current;
    if (!map) {
      return;
    }
    if (points.length === 0) {
      map.setCenter(DEFAULT_CENTER, DEFAULT_ZOOM, { duration: 250 });
      return;
    }
    if (points.length === 1) {
      map.setCenter(points[0].geometry, DEFAULT_ZOOM, { duration: 250 });
      return;
    }

    const lats = points.map((point) => point.geometry[0]);
    const lngs = points.map((point) => point.geometry[1]);
    const bounds = [
      [Math.min(...lats), Math.min(...lngs)],
      [Math.max(...lats), Math.max(...lngs)],
    ];
    map.setBounds(bounds, {
      checkZoomRange: true,
      zoomMargin: 40,
      duration: 250,
    });
  }, [points]);

  const defaultState = {
    center,
    zoom: DEFAULT_ZOOM,
  };

  return (
    <section className="yandex-map-section">
      <h3 className="yandex-map-section__title">Наше расположение</h3>
      <YMaps query={{ apikey: apiKey, lang: 'ru_RU' }}>
        <div style={{ width: '100%', height: '400px' }}>
          <Map defaultState={defaultState} width="100%" height="100%" instanceRef={mapRef}>
            {points.map((point) => (
              <Placemark
                key={point.id}
                geometry={point.geometry}
                modules={['geoObject.addon.balloon', 'geoObject.addon.hint']}
                properties={{
                  hintContent: point.name,
                  balloonContentHeader: point.name,
                  balloonContentBody: `${point.description}. ${point.address}`,
                  balloonContentFooter:
                    '<button type="button" '
                    + `data-restaurant-id="${point.domId}" `
                    + 'style="font-size: 12px; padding: 6px 10px; border: 1px solid #d1d5db; border-radius: 8px; background: #fff; cursor: pointer;">'
                    + 'Выбрать этот ресторан'
                    + '</button>',
                }}
                options={{
                  balloonAutoPan: true,
                }}
                instanceRef={(placemarkInstance) => bindPlacemarkEvents(placemarkInstance, point)}
              />
            ))}
          </Map>
        </div>
      </YMaps>
    </section>
  );
}
