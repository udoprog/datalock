package eu.toolchain.datalock;

import java.util.Optional;

public interface Credential {
  Optional<Long> getExpiresInSeconds();

  void refreshToken();

  Optional<String> getAccessToken();
}
