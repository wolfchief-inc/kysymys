package net.unit8.kysymys.avatar.image;

import com.talanlabs.avatargenerator.Avatar;
import com.talanlabs.avatargenerator.eightbit.EightBitAvatar;

/**
 * Builds deterministic 8-bit pixel avatars seeded by the user id. Same id
 * produces the same image — so re-generation is safe if the cached row is
 * lost.
 */
public class EightBitAvatarGenerator {
    private final Avatar female;
    private final Avatar male;

    public EightBitAvatarGenerator() {
        this.female = EightBitAvatar.newFemaleAvatarBuilder().size(64, 64).build();
        this.male = EightBitAvatar.newMaleAvatarBuilder().size(64, 64).build();
    }

    public byte[] generate(String seedId) {
        long seed = stableSeed(seedId);
        Avatar avatar = (seed % 2 == 0) ? female : male;
        return avatar.createAsPngBytes(seed);
    }

    private static long stableSeed(String s) {
        long h = 0L;
        for (int i = 0; i < s.length(); i++) {
            h = 31 * h + s.charAt(i);
        }
        return h;
    }
}
