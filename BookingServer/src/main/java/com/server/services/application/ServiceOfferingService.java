package com.server.services.application;

import com.server.services.api.ServiceDTO;
import com.server.services.domain.ServiceDuration;
import com.server.services.domain.ServiceName;
import com.server.services.domain.ServicePrice;
import com.server.services.domain.ServiceRepository;
import com.server.shared.infrastructure.UserMapper;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServiceOfferingService {

    private final ServiceRepository serviceRepository;
    private final UserMapper userMapper;


    public ServiceOfferingService(ServiceRepository serviceRepository, UserMapper userMapper) {
        this.serviceRepository = serviceRepository;
        this.userMapper = userMapper;
    }

    @Transactional(readOnly = true)
    public ServiceDTO getServiceById(int id) {
        com.server.services.domain.Service service = findServiceById(id);
        return userMapper.toDTO(service);
    }

    @Transactional(readOnly = true)
    public List<ServiceDTO> getAllServices() {
        List<com.server.services.domain.Service> services = serviceRepository.findAll();
        return services.stream().map(userMapper::toDTO).collect(Collectors.toList());
    }

    @Transactional
    public int addService(AddServiceCommand command) {
        com.server.services.domain.Service service = new com.server.services.domain.Service(
                0,
                new ServiceName(command.name()),
                command.organizationId(),
                command.description(),
                new ServiceDuration(command.durationMinutes()),
                new ServicePrice(command.price())
        );
        return serviceRepository.save(service).getId();
    }


    public void updateService(UpdateServiceCommand command) {
        com.server.services.domain.Service service = findServiceById(command.id());

        if (command.name() != null) {
            service.changeName(new ServiceName(command.name()));
        }
        if (command.description() != null && !command.description().isEmpty()) {
            service.changeDescription(command.description());
        }
        if (command.organizationId() != null) {
            service.changeOrganizationId(command.organizationId());
        }
        if (command.durationMinutes() != null) {
            service.changeDuration(new ServiceDuration(command.durationMinutes()));
        }
        if (command.price() != null) {
            service.changePrice(new ServicePrice(command.price()));
        }
        serviceRepository.save(service);
    }

    @Transactional
    public void deleteServiceById(int id) {
        com.server.services.domain.Service service = findServiceById(id);
        serviceRepository.delete(service);
    }

    private com.server.services.domain.Service findServiceById(int id) {
        return serviceRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Service with id: " + id + " is not found"
                ));
    }
}
