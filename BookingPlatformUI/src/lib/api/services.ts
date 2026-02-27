import {api} from './client';
import type {ServiceDTO} from '$lib/types';

interface CreateServiceRequest{
    name: string;
    organizationId: number;
    description: string;
    durationMinutes: number;
    price: number;
}
interface UpdateServiceRequest {
    name?: string;
    organizationId?: number;
    description?: string;
    durationMinutes?: number;
    price?: number;
}

export const servicesApi = {
    getAll: () => api.get<ServiceDTO[]>(`/api/services`),
    getById: (id: number) => api.get<ServiceDTO>(`/api/services/${id}`),
    create: (body: CreateServiceRequest) => api.post<number>(`/api/services`, body),
    delete: (id: number)=>api.delete<void>(`/api/services/${id}`),
    update: (id: number, body: UpdateServiceRequest)=>api.patch<void>(`/api/services/${id}`,body),
    getByOrganization: (orgId: number) =>
        api.get<ServiceDTO[]>(`/api/services`).then(all => all.filter(s => s.organizationId === orgId))
};
