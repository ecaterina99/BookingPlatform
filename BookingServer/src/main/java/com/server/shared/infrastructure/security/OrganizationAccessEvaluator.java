package com.server.shared.infrastructure.security;

import com.server.booking.domain.Booking;
import com.server.booking.domain.BookingRepository;
import com.server.organization.domain.enums.Role;
import com.server.organization.domain.organizationMembers.OrganizationMember;
import com.server.organization.domain.organizationMembers.OrganizationMemberRepository;
import com.server.service.domain.Service;
import com.server.service.domain.ServiceRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component("orgAccessEvaluator")
public class OrganizationAccessEvaluator {

    private final OrganizationMemberRepository memberRepository;
    private final BookingRepository bookingRepository;
    private final ServiceRepository serviceRepository;
    private final SecurityCurrentUserProvider currentUserProvider;

    public OrganizationAccessEvaluator(
            OrganizationMemberRepository memberRepository,
            BookingRepository bookingRepository,
            ServiceRepository serviceRepository,
            SecurityCurrentUserProvider currentUserProvider
    ) {
        this.memberRepository = memberRepository;
        this.bookingRepository = bookingRepository;
        this.serviceRepository = serviceRepository;
        this.currentUserProvider = currentUserProvider;
    }

    /**
     * Check if current user is an ADMIN of the specified organization
     */
    public boolean isOrganizationAdmin(int organizationId) {
        if (currentUserProvider.isGlobalAdmin()) {
            return true;
        }

        int userId = currentUserProvider.getUserId();
        Optional<OrganizationMember> member = memberRepository.findByOrganizationIdAndUserId(organizationId, userId);

        return member.isPresent() && member.get().getRole() == Role.ADMIN;
    }

    /**
     * Check if current user is a member (any role) of the specified organization
     */
    public boolean isOrganizationMember(int organizationId) {
        if (currentUserProvider.isGlobalAdmin()) {
            return true;
        }

        int userId = currentUserProvider.getUserId();
        Optional<OrganizationMember> member = memberRepository.findByOrganizationIdAndUserId(organizationId, userId);

        return member.isPresent();
    }

    /**
     * Check if current user is a SPECIALIST of the specified organization
     */
    public boolean isSpecialist(int organizationId) {
        if (currentUserProvider.isGlobalAdmin()) {
            return true;
        }

        int userId = currentUserProvider.getUserId();
        Optional<OrganizationMember> member = memberRepository.findByOrganizationIdAndUserId(organizationId, userId);

        return member.isPresent() && member.get().getRole() == Role.SPECIALIST;
    }

    /**
     * Check if current user can manage the specified booking
     * (either as the assigned specialist or as a global admin)
     */
    public boolean canManageBooking(int bookingId) {
        if (currentUserProvider.isGlobalAdmin()) {
            return true;
        }

        int userId = currentUserProvider.getUserId();
        Optional<Booking> booking = bookingRepository.findById(bookingId);

        return booking.isPresent() && booking.get().getSpecialistId() == userId;
    }

    /**
     * Check if current user owns the specified resource (is the same user)
     */
    public boolean isResourceOwner(int userId) {
        if (currentUserProvider.isGlobalAdmin()) {
            return true;
        }

        return currentUserProvider.getUserId() == userId;
    }

    /**
     * Check if current user is the client of the specified booking
     */
    public boolean isBookingClient(int bookingId) {
        if (currentUserProvider.isGlobalAdmin()) {
            return true;
        }

        int userId = currentUserProvider.getUserId();
        Optional<Booking> booking = bookingRepository.findById(bookingId);

        return booking.isPresent() && booking.get().getClientId() == userId;
    }

    /**
     * Check if current user can view the specified booking
     * (either as client, specialist, or global admin)
     */
    public boolean canViewBooking(int bookingId) {
        if (currentUserProvider.isGlobalAdmin()) {
            return true;
        }

        int userId = currentUserProvider.getUserId();
        Optional<Booking> booking = bookingRepository.findById(bookingId);

        if (booking.isEmpty()) {
            return false;
        }

        Booking b = booking.get();
        return b.getClientId() == userId || b.getSpecialistId() == userId;
    }

    /**
     * Check if current user can manage the specified service
     * (must be an ADMIN of the service's organization)
     */
    public boolean canManageService(int serviceId) {
        if (currentUserProvider.isGlobalAdmin()) {
            return true;
        }

        Optional<Service> service = serviceRepository.findById(serviceId);
        if (service.isEmpty()) {
            return false;
        }

        return isOrganizationAdmin(service.get().getOrganizationId());
    }
}
