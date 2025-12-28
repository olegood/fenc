package olegood.fenc.crypto;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.security.Key;
import java.security.KeyStore;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import olegood.fenc.crypto.kek.KekProperties;
import olegood.fenc.crypto.kek.KekService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;

@ExtendWith(MockitoExtension.class)
class KekServiceTest {

  private KekService kekService;

  @Mock
  private KeyStore mockKeyStore;

  @Mock
  private KekProperties mockKekProperties;

  @BeforeEach
  void setUp() throws Exception {
//    MockitoAnnotations.openMocks(this);

    // Stub properties
    when(mockKekProperties.password()).thenReturn("password");
    when(mockKekProperties.alias()).thenReturn("activeAlias");
    when(mockKekProperties.keyStoreType()).thenReturn("JCEKS");
    when(mockKekProperties.location()).thenReturn(mock(Resource.class));

    // Initialize class under test with mocked KeyStore
    doAnswer(invocation -> mockKeyStore).when(mockKeyStore).load(any(), any());

    kekService = new KekService(mockKekProperties);
  }

  @Test
  void testGetActiveKekSuccessfully() throws Exception {
    // Arrange
    char[] password = "password".toCharArray();
    String alias = "validAlias";
    SecretKey secretKey = new SecretKeySpec(new byte[32], "AES");
    KeyStore mockKeyStore = mock(KeyStore.class);
    when(mockKeyStore.containsAlias(alias)).thenReturn(true);
    when(mockKeyStore.getKey(alias, password)).thenReturn(secretKey);

    KekProperties mockProperties = mock(KekProperties.class);
    when(mockProperties.password()).thenReturn("password");
    when(mockProperties.alias()).thenReturn(alias);
    when(mockProperties.keyStoreType()).thenReturn("JCEKS");
    when(mockProperties.location()).thenReturn(mock(Resource.class));

    KekService kekService = new KekService(mockProperties);

    // Inject the mocked KeyStore
    var field = KekService.class.getDeclaredField("keyStore");
    field.setAccessible(true);
    field.set(kekService, mockKeyStore);

    // Act
    SecretKey activeKek = kekService.getActiveKek();

    // Assert
    assertNotNull(activeKek);
    assertEquals("AES", activeKek.getAlgorithm());
    assertEquals(32, activeKek.getEncoded().length);

    verify(mockKeyStore).containsAlias(alias);
    verify(mockKeyStore).getKey(alias, password);
  }

  @Test
  void testGetActiveKekFailsWhenAliasNotFound() throws Exception {
    // Arrange
    char[] password = "password".toCharArray();
    String alias = "missingAlias";
    KeyStore mockKeyStore = mock(KeyStore.class);
    when(mockKeyStore.containsAlias(alias)).thenReturn(false);

    KekProperties mockProperties = mock(KekProperties.class);
    when(mockProperties.password()).thenReturn("password");
    when(mockProperties.alias()).thenReturn(alias);
    when(mockProperties.keyStoreType()).thenReturn("JCEKS");
    when(mockProperties.location()).thenReturn(mock(Resource.class));

    KekService kekService = new KekService(mockProperties);

    // Inject the mocked KeyStore
    var field = KekService.class.getDeclaredField("keyStore");
    field.setAccessible(true);
    field.set(kekService, mockKeyStore);

    // Act & Assert
    IllegalStateException exception = assertThrows(IllegalStateException.class, kekService::getActiveKek);
    assertTrue(exception.getMessage().contains("KEK `" + alias + "` not found in keystore"));

    verify(mockKeyStore).containsAlias(alias);
    verify(mockKeyStore, never()).getKey(eq(alias), eq(password));
  }

  @Test
  void testGetActiveKekFailsForInvalidKeyType() throws Exception {
    // Arrange
    char[] password = "password".toCharArray();
    String alias = "invalidKeyAlias";
    KeyStore mockKeyStore = mock(KeyStore.class);
    Key invalidKey = mock(Key.class);

    when(mockKeyStore.containsAlias(alias)).thenReturn(true);
    when(mockKeyStore.getKey(alias, password)).thenReturn(invalidKey);

    KekProperties mockProperties = mock(KekProperties.class);
    when(mockProperties.password()).thenReturn("password");
    when(mockProperties.alias()).thenReturn(alias);
    when(mockProperties.keyStoreType()).thenReturn("JCEKS");
    when(mockProperties.location()).thenReturn(mock(Resource.class));

    KekService kekService = new KekService(mockProperties);

    // Inject the mocked KeyStore
    var field = KekService.class.getDeclaredField("keyStore");
    field.setAccessible(true);
    field.set(kekService, mockKeyStore);

    // Act & Assert
    IllegalStateException exception = assertThrows(IllegalStateException.class, kekService::getActiveKek);
    assertTrue(exception.getMessage().contains("KEK is not a SecretKey"));

    verify(mockKeyStore).containsAlias(alias);
    verify(mockKeyStore).getKey(alias, password);
  }

  @Test
  void testGetActiveKekFailsForNonAESKey() throws Exception {
    // Arrange
    char[] password = "password".toCharArray();
    String alias = "nonAESAlias";
    SecretKey nonAesKey = new SecretKeySpec(new byte[32], "DES"); // Incorrect algorithm
    KeyStore mockKeyStore = mock(KeyStore.class);

    when(mockKeyStore.containsAlias(alias)).thenReturn(true);
    when(mockKeyStore.getKey(alias, password)).thenReturn(nonAesKey);

    KekProperties mockProperties = mock(KekProperties.class);
    when(mockProperties.password()).thenReturn("password");
    when(mockProperties.alias()).thenReturn(alias);
    when(mockProperties.keyStoreType()).thenReturn("JCEKS");
    when(mockProperties.location()).thenReturn(mock(Resource.class));

    KekService kekService = new KekService(mockProperties);

    // Inject the mocked KeyStore
    var field = KekService.class.getDeclaredField("keyStore");
    field.setAccessible(true);
    field.set(kekService, mockKeyStore);

    // Act & Assert
    IllegalStateException exception = assertThrows(IllegalStateException.class, kekService::getActiveKek);
    assertTrue(exception.getMessage().contains("KEK algorithm must be AES"));

    verify(mockKeyStore).containsAlias(alias);
    verify(mockKeyStore).getKey(alias, password);
  }
}
