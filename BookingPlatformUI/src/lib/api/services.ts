import {api} from './client';
import type {ServiceDTO, ServiceCategoryType} from '$lib/types';

interface CreateServiceRequest{
    name: string;
    organizationId: number;
    description: string;
    durationMinutes: number;
    price: number;
    category: ServiceCategoryType;
}
interface UpdateServiceRequest {
    name?: string;
    organizationId?: number;
    description?: string;
    durationMinutes?: number;
    price?: number;
    category?: ServiceCategoryType;
}

export const servicesApi = {
    getAll: () => api.get<ServiceDTO[]>(`/api/services`),
    getByCategory: (category: ServiceCategoryType) => api.get<ServiceDTO[]>(`/api/services?category=${category}`),
    getById: (id: number) => api.get<ServiceDTO>(`/api/services/${id}`),
    create: (body: CreateServiceRequest) => api.post<number>(`/api/services`, body),
    delete: (id: number)=>api.delete<void>(`/api/services/${id}`),
    update: (id: number, body: UpdateServiceRequest)=>api.patch<void>(`/api/services/${id}`,body),
    getByOrganization: (orgId: number) =>
        api.get<ServiceDTO[]>(`/api/services?organizationId=${orgId}`)
};
