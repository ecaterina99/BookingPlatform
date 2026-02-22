import {api} from './client';
import type {OrganizationDTO} from "$lib/types";

export const organizationsApi = {
    getAll: () => api.get<OrganizationDTO[]>('/api/organizations'),
    getById: (id: number) => api.get<OrganizationDTO>(`/api/organizations/${id}`)
}