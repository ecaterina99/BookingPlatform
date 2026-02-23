import {api} from './client';
import type {ServiceDTO} from '$lib/types';

export const servicesApi = {
    getAll: () => api.get<ServiceDTO[]>(`/api/services`),
    getById: (id : number)=> api.get<ServiceDTO>(`/api/services/${id}` )
};