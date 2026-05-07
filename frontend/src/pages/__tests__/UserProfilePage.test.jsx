import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import UserProfilePage from '../UserProfilePage.jsx';

const mockFetchUserProfile = vi.fn();
const mockUpdateUserProfile = vi.fn();

vi.mock('../../services/userApi.js', () => ({
  fetchUserProfile: (...args) => mockFetchUserProfile(...args),
  updateUserProfile: (...args) => mockUpdateUserProfile(...args),
}));

describe('UserProfilePage', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('shows loading then renders profile form and addresses', async () => {
    mockFetchUserProfile.mockResolvedValueOnce({
      id: 1,
      fullName: 'User One',
      email: 'u@test.local',
      phone: '+79990000000',
      addresses: [{ id: 10, label: 'Home', address: 'Lenina 1', apartment: '42' }],
    });

    render(<UserProfilePage />);

    expect(screen.getByText('Загрузка...')).toBeInTheDocument();
    await waitFor(() => expect(screen.getByDisplayValue('User One')).toBeInTheDocument());
    expect(screen.getByDisplayValue('u@test.local')).toBeDisabled();
    expect(screen.getByText('Home')).toBeInTheDocument();
    expect(screen.getByText('Lenina 1')).toBeInTheDocument();
    expect(screen.getByText('Квартира: 42')).toBeInTheDocument();
  });

  it('shows empty addresses state and submits changed profile fields', async () => {
    mockFetchUserProfile.mockResolvedValueOnce({
      id: 1,
      fullName: '',
      email: 'u@test.local',
      phone: '',
      addresses: [],
    });
    mockUpdateUserProfile.mockResolvedValueOnce({
      id: 1,
      fullName: 'Updated User',
      email: 'u@test.local',
      phone: '+79991112233',
      addresses: [],
    });

    render(<UserProfilePage />);

    await waitFor(() => expect(screen.getByText('Адресов пока нет.')).toBeInTheDocument());
    await userEvent.type(screen.getByLabelText('Имя'), 'Updated User');
    await userEvent.type(screen.getByLabelText('Телефон'), '+79991112233');
    await userEvent.click(screen.getByRole('button', { name: 'Сохранить' }));

    await waitFor(() => expect(screen.getByRole('status')).toBeInTheDocument());
    expect(mockUpdateUserProfile).toHaveBeenCalledWith({
      fullName: 'Updated User',
      phone: '+79991112233',
    });
    expect(screen.getByDisplayValue('Updated User')).toBeInTheDocument();
  });

  it('shows load and save errors', async () => {
    mockFetchUserProfile.mockRejectedValueOnce(new Error('Load failed'));
    const { unmount } = render(<UserProfilePage />);

    await waitFor(() => expect(screen.getByRole('alert')).toBeInTheDocument());
    expect(screen.getByText('Load failed')).toBeInTheDocument();
    unmount();

    mockFetchUserProfile.mockResolvedValueOnce({
      id: 1,
      fullName: 'User One',
      email: 'u@test.local',
      phone: '+79990000000',
      addresses: [],
    });
    mockUpdateUserProfile.mockRejectedValueOnce(new Error('Save failed'));

    render(<UserProfilePage />);
    await waitFor(() => expect(screen.getByDisplayValue('User One')).toBeInTheDocument());
    await userEvent.click(screen.getByRole('button', { name: 'Сохранить' }));

    await waitFor(() => expect(screen.getByText('Save failed')).toBeInTheDocument());
  });
});
