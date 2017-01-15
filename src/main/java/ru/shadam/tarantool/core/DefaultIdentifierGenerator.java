package ru.shadam.tarantool.core;

import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.data.keyvalue.core.IdentifierGenerator;
import org.springframework.data.util.TypeInformation;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author sala
 */
public enum DefaultIdentifierGenerator implements IdentifierGenerator {
    INSTANCE;

    private final AtomicReference<SecureRandom> secureRandom = new AtomicReference<SecureRandom>(null);

    /*
     * (non-Javadoc)
     * @see org.springframework.data.keyvalue.core.IdentifierGenerator#newIdForType(org.springframework.data.util.TypeInformation)
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> T generateIdentifierOfType(TypeInformation<T> identifierType) {

        Class<?> type = identifierType.getType();

        if (ClassUtils.isAssignable(UUID.class, type)) {
            return (T) UUID.randomUUID();
        } else if (ClassUtils.isAssignable(String.class, type)) {
            return (T) UUID.randomUUID().toString();
        } else if (ClassUtils.isAssignable(Integer.class, type)) {
            return (T) Integer.valueOf(getSecureRandom().nextInt());
        } else if (ClassUtils.isAssignable(Long.class, type)) {
            return (T) Long.valueOf(getSecureRandom().nextLong());
        }

        throw new InvalidDataAccessApiUsageException("Non gereratable id type....");
    }

    private SecureRandom getSecureRandom() {

        SecureRandom secureRandom = this.secureRandom.get();
        if (secureRandom != null) {
            return secureRandom;
        }

        for (String algorithm : OsTools.secureRandomAlgorithmNames()) {
            try {
                secureRandom = SecureRandom.getInstance(algorithm);
            } catch (NoSuchAlgorithmException e) {
                // ignore and try next.
            }
        }

        if (secureRandom == null) {
            throw new InvalidDataAccessApiUsageException(
                    String.format("Could not create SecureRandom instance for one of the algorithms '%s'.",
                            StringUtils.collectionToCommaDelimitedString(OsTools.secureRandomAlgorithmNames())));
        }

        this.secureRandom.compareAndSet(null, secureRandom);

        return secureRandom;
    }

    /**
     * @author Christoph Strobl
     * @since 1.1.2
     */
    private static class OsTools {

        private static final String OPERATING_SYSTEM_NAME = System.getProperty("os.name").toLowerCase();

        private static final List<String> SECURE_RANDOM_ALGORITHMS_LINUX_OSX_SOLARIS = Arrays.asList("NativePRNGBlocking",
                "NativePRNGNonBlocking", "NativePRNG", "SHA1PRNG");
        private static final List<String> SECURE_RANDOM_ALGORITHMS_WINDOWS = Arrays.asList("SHA1PRNG", "Windows-PRNG");

        static List<String> secureRandomAlgorithmNames() {
            return OPERATING_SYSTEM_NAME.indexOf("win") >= 0 ? SECURE_RANDOM_ALGORITHMS_WINDOWS
                    : SECURE_RANDOM_ALGORITHMS_LINUX_OSX_SOLARIS;
        }
    }
}
