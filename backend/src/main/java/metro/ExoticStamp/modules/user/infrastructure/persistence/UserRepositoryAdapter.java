package metro.ExoticStamp.modules.user.infrastructure.persistence;

import metro.ExoticStamp.common.model.PageQuery;
import metro.ExoticStamp.common.model.PageResult;
import metro.ExoticStamp.modules.user.domain.model.User;
import metro.ExoticStamp.modules.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

    private final JpaUserRepository jpa;

    @Override public User save(User u)                        { return jpa.save(u); }
    @Override public Optional<User> findById(UUID uuid)         { return jpa.findById(uuid); }
    @Override public Optional<User> findByEmail(String e)       { return jpa.findByEmail(e); }
    @Override public Optional<User> findByUsername(String u)    { return jpa.findByUsername(u); }
    @Override public boolean existsByEmail(String e)             { return jpa.existsByEmail(e); }
    @Override public boolean existsByUsername(String u)          { return jpa.existsByUsername(u); }
    @Override public boolean existsByPhoneNumber(String p)       { return jpa.existsByPhoneNumber(p); }
    @Override public boolean existsById(UUID uuid)               { return jpa.existsById(uuid); }
    @Override public void deleteById(UUID uuid)                  { jpa.deleteById(uuid); }

    @Override
    public PageResult<User> findAll(PageQuery query) {
        Page<User> page = jpa.findAll(PageRequest.of(query.page(), query.size()));
        return PageResult.of(
                page.getContent(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber()
        );
    }

    @Override
    public Optional<Long> findTokenVersionById(UUID id) {
        return jpa.findTokenVersionById(id);
    }

    @Override
    public int incrementTokenVersionById(UUID id) {
        return jpa.incrementTokenVersionById(id);
    }
}
