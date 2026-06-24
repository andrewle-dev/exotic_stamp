package metro.ExoticStamp.modules.rbac.application;

import metro.ExoticStamp.modules.rbac.application.mapper.RoleAppMapper;
import metro.ExoticStamp.modules.rbac.application.view.PermissionView;
import metro.ExoticStamp.modules.rbac.domain.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PermissionQueryService {

    private final PermissionRepository permissionRepository;

    @Transactional(readOnly = true)
    public List<PermissionView> listPermissions() {
        return permissionRepository.findAllOrderByPermission().stream()
                .map(RoleAppMapper::toPermissionView)
                .toList();
    }
}
