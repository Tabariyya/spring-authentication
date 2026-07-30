package com.tabariyya.authentication.token;

import com.tabariyya.authentication.dto.TokenResponse;
import com.tabariyya.authentication.models.BaseUser;
import com.tabariyya.utils.jwt.JwtProducer;
import com.tabariyya.utils.jwt.TokenType;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TokenIssuerTest {

    static class TestUser implements BaseUser<Integer> {
        private Integer id;
        private String userName;
        private String password;
        private String contactInfo;
        private String contactInfoType;

        TestUser(Integer id, String userName) {
            this.id = id;
            this.userName = userName;
        }

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getContactInfo() {
            return contactInfo;
        }

        public void setContactInfo(String contactInfo) {
            this.contactInfo = contactInfo;
        }

        public String getContactInfoType() {
            return contactInfoType;
        }

        public void setContactInfoType(String contactInfoType) {
            this.contactInfoType = contactInfoType;
        }
    }

    @Mock
    JwtProducer jwtProducer;

    TokenIssuer<TestUser, Integer> tokenIssuer;

    @BeforeEach
    void setUp() {
        tokenIssuer = new TokenIssuer<>(jwtProducer, "PT15M", "P7D");
    }

    @Test
    @SuppressWarnings("unchecked")
    void generateAccessToken_carriesSubjectOnly_withAccessTypeAndExpiry() {
        TestUser user = new TestUser(42, "alperen");
        when(jwtProducer.generateToken(any(Map.class), any(Duration.class), any(TokenType.class)))
                .thenReturn("access-tok");

        String token = tokenIssuer.generateAccessToken(user);

        assertEquals("access-tok", token);

        ArgumentCaptor<Map<String, Object>> claimsCaptor = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Duration> expiryCaptor = ArgumentCaptor.forClass(Duration.class);
        ArgumentCaptor<TokenType> typeCaptor = ArgumentCaptor.forClass(TokenType.class);

        org.mockito.Mockito.verify(jwtProducer)
                .generateToken(claimsCaptor.capture(), expiryCaptor.capture(), typeCaptor.capture());

        Map<String, Object> claims = claimsCaptor.getValue();
        assertEquals("42", claims.get(Claims.SUBJECT));
        assertFalse(claims.containsKey("username"));
        assertEquals(Duration.parse("PT15M"), expiryCaptor.getValue());
        assertEquals(TokenType.ACCESS, typeCaptor.getValue());
    }

    @Test
    @SuppressWarnings("unchecked")
    void generateRefreshToken_carriesSubjectOnly_withRefreshTypeAndExpiry() {
        TestUser user = new TestUser(7, "hasan");
        when(jwtProducer.generateToken(any(Map.class), any(Duration.class), any(TokenType.class)))
                .thenReturn("refresh-tok");

        String token = tokenIssuer.generateRefreshToken(user);

        assertEquals("refresh-tok", token);

        ArgumentCaptor<Map<String, Object>> claimsCaptor = ArgumentCaptor.forClass(Map.class);
        ArgumentCaptor<Duration> expiryCaptor = ArgumentCaptor.forClass(Duration.class);
        ArgumentCaptor<TokenType> typeCaptor = ArgumentCaptor.forClass(TokenType.class);

        org.mockito.Mockito.verify(jwtProducer)
                .generateToken(claimsCaptor.capture(), expiryCaptor.capture(), typeCaptor.capture());

        Map<String, Object> claims = claimsCaptor.getValue();
        assertEquals("7", claims.get(Claims.SUBJECT));
        assertFalse(claims.containsKey("username"));
        assertEquals(Duration.parse("P7D"), expiryCaptor.getValue());
        assertEquals(TokenType.REFRESH, typeCaptor.getValue());
    }

    @Test
    @SuppressWarnings("unchecked")
    void issueTokens_returnsRefreshThenAccess() {
        TestUser user = new TestUser(1, "mehmet");
        when(jwtProducer.generateToken(any(Map.class), any(Duration.class), eq(TokenType.ACCESS)))
                .thenReturn("access-tok");
        when(jwtProducer.generateToken(any(Map.class), any(Duration.class), eq(TokenType.REFRESH)))
                .thenReturn("refresh-tok");

        TokenResponse response = tokenIssuer.issueTokens(user);

        assertEquals("refresh-tok", response.refreshToken());
        assertEquals("access-tok", response.accessToken());
    }
}
