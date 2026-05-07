import { render, screen } from '@testing-library/react';
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
  Placemark: ({ geometry, properties, instanceRef }) => {
    const events = {
      add: vi.fn((eventName, handler) => {
        placemarkEvents.push({ eventName, handler });
      }),
    };
    instanceRef({ events });
    return (
      <button type="button" data-testid="placemark" data-geometry={geometry.join(',')}>
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
  });

  it('centers default map when restaurants list is empty', () => {
    render(<YandexMap restaurants={[]} />);

    expect(screen.getByTestId('ymaps')).toHaveAttribute('data-lang', 'ru_RU');
    expect(screen.getByTestId('map')).toHaveAttribute('data-center', '55.751574,37.573856');
    expect(mapApi.setCenter).toHaveBeenCalledWith([55.7558, 37.6173], 10, { duration: 250 });
    expect(screen.queryAllByTestId('placemark')).toHaveLength(0);
  });

  it('renders valid restaurant markers and skips invalid coordinates', () => {
    render(
      <YandexMap
        restaurants={[
          { id: 1, name: 'Pizza', lat: '55.7', lng: '37.6', address: 'Lenina 1', cuisineType: 'Italian' },
          { id: 2, name: 'Broken', lat: null, lng: null },
        ]}
      />
    );

    expect(screen.getAllByTestId('placemark')).toHaveLength(1);
    expect(screen.getByTestId('placemark')).toHaveAttribute('data-geometry', '55.7,37.6');
    expect(screen.getByText('Pizza')).toBeInTheDocument();
    expect(screen.getByText(/Italian\. Lenina 1/)).toBeInTheDocument();
    expect(mapApi.setCenter).toHaveBeenCalledWith([55.7, 37.6], 10, { duration: 250 });
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

    const clickEvent = placemarkEvents.find((event) => event.eventName === 'click');
    clickEvent.handler();
    expect(onSelectRestaurant).toHaveBeenCalledWith(expect.objectContaining({ id: 1 }));

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
