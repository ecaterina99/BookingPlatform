import {api} from './client';
import type {OrganizationDTO, SpecialistDTO, OrganizationMemberDTO} from "$lib/types";

export const organizationsApi = {
    getAll: () => api.get<OrganizationDTO[]>('/api/organizations'),
    getById: (id: number) => api.get<OrganizationDTO>(`/api/organizations/${id}`),
    getSpecialists: (orgId: number) => api.get<SpecialistDTO[]>(`/api/members/${orgId}/specialists`),
    getMembership: ()=> api.get<OrganizationMemberDTO[]>(`/api/members/me`)
}