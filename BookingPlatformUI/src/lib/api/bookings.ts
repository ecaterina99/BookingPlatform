import { api } from './client';
import type { BookingDTO } from '$lib/types';

interface CreateBookingRequest {
    clientId: number;
    specialistId: number;
    serviceId: number;
    start: string;
}

export const bookingsApi = {
    getByClient:      (clientId: number) => api.get<BookingDTO[]>(`/api/bookings/client/${clientId}`),
    getById:          (id: number) => api.get<BookingDTO>(`/api/bookings/${id}`),
    create:           (body: CreateBookingRequest) => api.post<number>('/api/bookings', body),
    cancelAsClient:   (id: number) => api.patch<void>(`/api/bookings/${id}/cancel/client`, {})
};
