package metro.ExoticStamp.modules.user.application;

import metro.ExoticStamp.common.model.PageQuery;
import metro.ExoticStamp.common.model.PageResult;
import metro.ExoticStamp.modules.user.application.mapper.UserAppMapper;
import metro.ExoticStamp.modules.user.application.port.UserCachePort;
import metro.ExoticStamp.modules.user.application.view.UserView;
import metro.ExoticStamp.modules.user.domain.exception.UserNotFoundException;
import metro.ExoticStamp.modules.user.domain.model.User;
import metro.ExoticStamp.modules.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserQueryService {

    private final UserRepository userRepository;
    private final UserCachePort cachePort;

    @Transactional(readOnly = true)
    public UserView getById(UUID id) {
        return cachePort.get(id)
                .orElseGet(() -> {
                    UserView res = userRepository.findById(id)
                            .map(UserAppMapper::toView)
                            .orElseThrow(() -> new UserNotFoundException(id));
                    cachePort.put(id, res);
                    return res;
                });
    }

    @Transactional(readOnly = true)
    public UserView getByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(UserAppMapper::toView)
                .orElseThrow(() -> new UserNotFoundException("username", username));
    }

    @Transactional(readOnly = true)
    public UserView getByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(UserAppMapper::toView)
                .orElseThrow(() -> new UserNotFoundException("email", email));
    }

    @Transactional(readOnly = true)
    public PageResult<UserView> getAll(PageQuery query) {
        PageResult<User> page = userRepository.findAll(query);
        List<UserView> responses = page.content().stream()
                .map(UserAppMapper::toView)
                .toList();
        return PageResult.of(responses, page.totalElements(), page.totalPages(), page.currentPage());
    }
}
