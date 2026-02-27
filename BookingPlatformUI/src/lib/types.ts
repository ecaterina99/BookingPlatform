//when the API returns a booking, it will have exactly these fields with these types.
//we store in lib reusable features

export interface AuthResponse {
    token: string;
    tokenType: string;
    expiresIn: number;
}

export interface UserDTO {
    id: number;
    email: string;
    fullName: string;
    globalRole: 'USER' | 'GLOBAL_ADMIN';
}

export interface OrganizationDTO {
    id: number;
    name: string;
    city: string;
    address: string;
    phone: string;
    email: string;
}

export interface ServiceDTO {
    id: number;
    name: string;
    organizationId: number;
    description: string;
    durationMinutes: number;
    price: number;
}

export interface BookingDTO {
    id: number;
    clientId: number;
    specialistId: number;
    serviceId: number;
    start: string;
    end: string;
    status: 'PENDING' | 'CONFIRMED' | 'CANCELLED';
}

export interface SpecialistDTO {
    userId: number,
    fullName: string;
}

export interface OrganizationMemberDTO {
    id: number,
    organizationId: number;
    userId: number;
    role: 'ADMIN' | 'SPECIALIST';
}