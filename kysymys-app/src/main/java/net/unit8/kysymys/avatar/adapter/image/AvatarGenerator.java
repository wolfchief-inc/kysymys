package net.unit8.kysymys.avatar.adapter.image;

import com.talanlabs.avatargenerator.Avatar;
import com.talanlabs.avatargenerator.eightbit.EightBitAvatar;
import net.unit8.kysymys.avatar.application.GenerateAvatarPort;
import org.springframework.stereotype.Component;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

@Component
class AvatarGenerator implements GenerateAvatarPort {
    private final Avatar female;
    private final Avatar male;
    private final SecureRandom random;

    public AvatarGenerator() throws NoSuchAlgorithmException {
        female= EightBitAvatar.newFemaleAvatarBuilder()
                .size(64, 64)
                .build();
        male= EightBitAvatar.newMaleAvatarBuilder()
                .size(64, 64)
                .build();
        random = SecureRandom.getInstance("SHA1PRNG");
    }

    @Override
    public byte[] generate() {
        Avatar avatar = random.nextInt() % 2 == 0 ? female : male;
        return avatar.createAsPngBytes(random.nextLong());
    }
}
