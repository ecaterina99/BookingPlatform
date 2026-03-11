import {api} from './client';
import type {OrganizationDTO, SpecialistDTO, OrganizationMemberDTO} from "$lib/types";

interface AddMemberRequest {
    userId: number;
    role: 'SPECIALIST' | 'ADMIN';
}

export const organizationsApi = {
    getAll: () => api.get<OrganizationDTO[]>('/api/organizations'),
    getById: (id: number) => api.get<OrganizationDTO>(`/api/organizations/${id}`),
    getSpecialists: (orgId: number) => api.get<SpecialistDTO[]>(`/api/members/${orgId}/specialists`),
    getMembership: ()=> api.get<OrganizationMemberDTO[]>(`/api/members/me`),
    getMembers: (orgId: number) => api.get<OrganizationMemberDTO[]>(`/api/members/${orgId}`),
    addMember: (orgId: number, body: AddMemberRequest) => api.post<number>(`/api/members/add/${orgId}`, body),
    removeMember: (orgId: number, userId: number) => api.delete<void>(`/api/members/${orgId}/${userId}`)
}