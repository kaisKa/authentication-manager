package ava.io.authentication_manager.services;

import ava.io.authentication_manager.db.entities.UserAccount;
import ava.io.authentication_manager.db.repositories.BaseRepository;
import ava.io.authentication_manager.db.repositories.UserAccountRepo;
import ava.io.authentication_manager.utils.ErrorCode;
import ava.io.authentication_manager.utils.custom_excpeption.UserNotFoundException;
import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Getter
public class UserAccountService extends BaseService<UserAccount> {

    private final UserAccountRepo userAccountRepo;

    public UserAccountService(BaseRepository<UserAccount> baseRepository, UserAccountRepo userAccountRepo) {
        super(baseRepository);
        this.userAccountRepo = userAccountRepo;
    }

    public  void save(UserAccount userAccount){
        userAccountRepo.save(userAccount);
    }

    @SneakyThrows
    public UserAccount update(UserAccount user) {
        if (!userAccountRepo.existsById(user.getId())) {
            throw new UserNotFoundException(ErrorCode.USER_NOT_FOUND.getMessage());
        }
        return updateAndPublishEvent(user);
    }


//    public void setAccountId(UUID userId, Integer accountId) {
//        Optional<UserAccount> user = userAccountRepo.findByKeycloakId(userId);
//        if (user.isPresent()) {
//            UserAccount u = user.get();
//
//            u.setAccountId(accountId);
//            update(u);
//        }
//
//    }

//    @SneakyThrows
//    public UUID getKeycloakUserId(UUID userId) {
//        var user = userAccountRepo.findById(userId);
//        if (user.isPresent()) {
//            return user.get().getKeycloakId();
//        }
//        throw new UserNotFoundException(ErrorCode.USER_NOT_FOUND.getMessage());
//    }

//    public Optional<UserAccount> getByKeycloakId(UUID kId){
//        return userAccountRepo.findByKeycloakId(kId);
//    }
//    public List<UserAccount> getUserByEmailAddress(String email){
//        return userAccountRepo.findByEmailAddress(email);
//    }
//
//    public List<UserAccount> getUserByPhone(String gsm){
//        return userAccountRepo.findByPhone(gsm);
//    }


}
