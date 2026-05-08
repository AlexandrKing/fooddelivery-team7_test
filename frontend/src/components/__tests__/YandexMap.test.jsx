import { fireEvent, render, screen, waitFor } from '@testing-library/react';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import YandexMap from '../YandexMap.jsx';

const mapApi = {
  setCenter: vi.fn(),
  setBounds: vi.fn(),
  panTo: vi.fn(),
  container: {
    getElement: vi.fn(),
  },
};
const placemarkEvents = [];

vi.mock('@pbe/react-yandex-maps', () => ({
  YMaps: ({ children, query }) => (
    <div data-testid="ymaps" data-apikey={query.apikey || ''} data-lang={query.lang}>
      {children}
    </div>
  ),
  Map: ({ children, defaultState, instanceRef }) => {
    instanceRef.current = mapApi;
    return (
      <div data-testid="map" data-center={defaultState.center.join(',')} data-zoom={defaultState.zoom}>
        {children}
      </div>
    );
  },
  Placemark: ({ geometry, modules, options, properties, instanceRef }) => {
    const events = {
      add: vi.fn((eventName, handler) => {
        placemarkEvents.push({ eventName, handler });
      }),
    };
    instanceRef?.({ events });
    return (
      <button
        type="button"
        data-testid="placemark"
        data-geometry={geometry.join(',')}
        data-balloon-body={properties.balloonContentBody}
        data-icon-image-href={options.iconImageHref}
        data-icon-image-offset={options.iconImageOffset.join(',')}
        data-icon-image-size={options.iconImageSize.join(',')}
        data-icon-layout={options.iconLayout}
        data-modules={modules.join(',')}
        data-hide-icon-on-balloon-open={String(options.hideIconOnBalloonOpen)}
        data-z-index={String(options.zIndex)}
      >
        {properties.hintContent}
        <span>{properties.balloonContentBody}</span>
        <span>{properties.balloonContentFooter}</span>
      </button>
    );
  },
}));

describe('YandexMap', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    placemarkEvents.length = 0;
    mapApi.container.getElement.mockReturnValue(document.body);
    Object.defineProperty(navigator, 'geolocation', {
      configurable: true,
      value: undefined,
    });
    import.meta.env.VITE_YANDEX_MAPS_API_KEY = 'test-api-key';
  });

  it('does not load Yandex Maps when API key is missing', () => {
    import.meta.env.VITE_YANDEX_MAPS_API_KEY = '';

    render(<YandexMap restaurants={[]} />);

    expect(screen.getByRole('alert')).toHaveTextContent('VITE_YANDEX_MAPS_API_KEY');
    expect(screen.queryByTestId('ymaps')).not.toBeInTheDocument();
    expect(screen.queryByTestId('map')).not.toBeInTheDocument();
  });

  it('centers default map when restaurants list is empty', () => {
    render(<YandexMap restaurants={[]} />);

    expect(screen.getByTestId('ymaps')).toHaveAttribute('data-lang', 'ru_RU');
    expect(screen.getByTestId('ymaps')).toHaveAttribute('data-apikey', 'test-api-key');
    expect(screen.getByTestId('map')).toHaveAttribute('data-center', '55.751574,37.573856');
    expect(mapApi.setCenter).toHaveBeenCalledWith([55.7558, 37.6173], 10, { duration: 250 });
    expect(screen.queryAllByTestId('placemark')).toHaveLength(0);
  });

  it('renders valid restaurant markers and skips invalid coordinates', () => {
    render(
      <YandexMap
        restaurants={[
          {
            id: 1,
            name: 'Pizza',
            lat: '55.7',
            lng: '37.6',
            address: 'Lenina 1',
            cuisineType: 'Italian',
            deliveryTime: 25,
          },
          { id: 2, name: 'Broken', lat: null, lng: null },
        ]}
      />
    );

    expect(screen.getAllByTestId('placemark')).toHaveLength(1);
    expect(screen.getByTestId('placemark')).toHaveAttribute('data-geometry', '55.7,37.6');
    expect(screen.getByTestId('placemark')).toHaveAttribute('data-icon-layout', 'default#image');
    expect(screen.getByTestId('placemark')).toHaveAttribute(
      'data-modules',
      'geoObject.addon.balloon,geoObject.addon.hint'
    );
    expect(screen.getByTestId('placemark')).toHaveAttribute('data-icon-image-size', '40,52');
    expect(screen.getByTestId('placemark')).toHaveAttribute('data-icon-image-offset', '-20,-52');
    expect(screen.getByTestId('placemark')).toHaveAttribute(
      'data-icon-image-href',
      expect.stringContaining('data:image/svg+xml')
    );
    expect(screen.getByTestId('placemark')).toHaveAttribute('data-hide-icon-on-balloon-open', 'false');
    expect(screen.getByTestId('placemark')).toHaveAttribute('data-z-index', '2000');
    expect(screen.getByText('Pizza')).toBeInTheDocument();
    expect(screen.getByTestId('placemark')).toHaveAttribute(
      'data-balloon-body',
      expect.stringContaining('<strong>Адрес:</strong> Lenina 1')
    );
    expect(screen.getByTestId('placemark')).toHaveAttribute(
      'data-balloon-body',
      expect.stringContaining('<strong>Кухня:</strong> Italian')
    );
    expect(screen.getByTestId('placemark')).toHaveAttribute(
      'data-balloon-body',
      expect.stringContaining('<strong>Доставка:</strong> до 25 мин')
    );
    expect(mapApi.setCenter).toHaveBeenCalledWith([55.7, 37.6], 10, { duration: 250 });
  });

  it('requests and renders user location on demand', async () => {
    const getCurrentPosition = vi.fn((onSuccess) => {
      onSuccess({
        coords: {
          accuracy: 18,
          latitude: 55.8,
          longitude: 37.7,
        },
      });
    });
    Object.defineProperty(navigator, 'geolocation', {
      configurable: true,
      value: { getCurrentPosition },
    });

    render(<YandexMap restaurants={[]} />);

    fireEvent.click(screen.getByRole('button', { name: 'Мое местоположение' }));

    await waitFor(() => expect(getCurrentPosition).toHaveBeenCalledTimes(1));
    await waitFor(() => expect(screen.getAllByTestId('placemark')).toHaveLength(1));
    expect(screen.getByTestId('placemark')).toHaveAttribute('data-geometry', '55.8,37.7');
    expect(screen.getByTestId('placemark')).toHaveAttribute('data-icon-image-size', '42,42');
    expect(screen.getByTestId('placemark')).toHaveAttribute('data-icon-image-offset', '-21,-21');
    expect(screen.getByTestId('placemark')).toHaveAttribute('data-z-index', '3000');
    expect(screen.getByText('Вы здесь')).toBeInTheDocument();
    expect(screen.getByText('Показать меня')).toBeInTheDocument();
    expect(mapApi.setCenter).toHaveBeenCalledWith([55.8, 37.7], 14, {
      duration: 250,
      checkZoomRange: true,
    });
  });

  it('shows an error when user location is blocked', async () => {
    Object.defineProperty(navigator, 'geolocation', {
      configurable: true,
      value: {
        getCurrentPosition: vi.fn((_, onError) => {
          onError({ code: 1 });
        }),
      },
    });

    render(<YandexMap restaurants={[]} />);

    fireEvent.click(screen.getByRole('button', { name: 'Мое местоположение' }));

    expect(await screen.findByRole('alert')).toHaveTextContent(
      'Доступ к геолокации запрещен в браузере.'
    );
    expect(screen.queryAllByTestId('placemark')).toHaveLength(0);
  });

  it('fits bounds for multiple restaurants and invokes select callbacks', () => {
    const onSelectRestaurant = vi.fn();
    render(
      <YandexMap
        restaurants={[
          { id: 1, name: 'Pizza', latitude: 55, longitude: 37 },
          { id: 2, name: 'Sushi', latitude: 56, longitude: 38 },
        ]}
        onSelectRestaurant={onSelectRestaurant}
      />
    );

    expect(mapApi.setBounds).toHaveBeenCalledWith(
      [
        [55, 37],
        [56, 38],
      ],
      { checkZoomRange: true, zoomMargin: 40, duration: 250 }
    );

    expect(placemarkEvents.some((event) => event.eventName === 'click')).toBe(false);

    document.body.innerHTML += '<button data-restaurant-id="1">Open</button>';
    const balloonOpenEvent = placemarkEvents.find((event) => event.eventName === 'balloonopen');
    balloonOpenEvent.handler();
    document.querySelector('[data-restaurant-id="1"]').click();

    expect(mapApi.panTo).toHaveBeenCalledWith([55, 37], {
      duration: 250,
      flying: true,
      checkZoomRange: true,
    });
    expect(onSelectRestaurant).toHaveBeenCalledWith(expect.objectContaining({ id: 1 }));
  });

  it('ignores missing map container and duplicate placemark refs safely', () => {
    mapApi.container.getElement.mockReturnValue(null);
    render(<YandexMap restaurants={[{ name: 'Nameless', lat: 55, lng: 37 }]} />);

    const balloonOpenEvent = placemarkEvents.find((event) => event.eventName === 'balloonopen');
    expect(() => balloonOpenEvent.handler()).not.toThrow();
    expect(screen.getByText('Nameless')).toBeInTheDocument();
  });
});
