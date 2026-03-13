import {api} from './client';
import type {OrganizationDTO, SpecialistDTO, OrganizationMemberDTO} from "$lib/types";

interface AddMemberRequest {
    userId: number;
    role: 'SPECIALIST' | 'ADMIN';
}

export interface CreateOrganizationRequest {
    name: string;
    city: string;
    address: string;
    email: string;
    phone: string;
}

export interface UpdateOrganizationRequest {
    name?: string;
    city?: string;
    address?: string;
    email?: string;
    phone?: string;
}

export const organizationsApi = {
    getAll: () => api.get<OrganizationDTO[]>('/api/organizations'),
    getById: (id: number) => api.get<OrganizationDTO>(`/api/organizations/${id}`),
    create: (body: CreateOrganizationRequest) => api.post<number>('/api/organizations', body),
    update: (id: number, body: UpdateOrganizationRequest) => api.patch<void>(`/api/organizations/${id}`, body),
    deleteOrg: (id: number) => api.delete<void>(`/api/organizations/${id}`),
    getSpecialists: (orgId: number) => api.get<SpecialistDTO[]>(`/api/members/${orgId}/specialists`),
    getMembership: ()=> api.get<OrganizationMemberDTO[]>(`/api/members/me`),
    getMembers: (orgId: number) => api.get<OrganizationMemberDTO[]>(`/api/members/${orgId}`),
    addMember: (orgId: number, body: AddMemberRequest) => api.post<number>(`/api/members/add/${orgId}`, body),
    changeMemberRole: (orgId: number, userId: number, role: 'SPECIALIST' | 'ADMIN') =>
        api.patch<void>(`/api/members/${orgId}/${userId}/role`, {role}),
    removeMember: (orgId: number, userId: number) => api.delete<void>(`/api/members/${orgId}/${userId}`)
}