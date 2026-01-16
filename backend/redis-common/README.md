# Redis Common Module

Module chung cung cấp các cấu hình và service Redis cho các microservice trong hệ thống MyBlog.

## Mục đích

Module này tập trung hóa các chức năng liên quan đến Redis, giúp:

- Tránh duplicate code giữa các service
- Đảm bảo cấu hình Redis nhất quán
- Dễ dàng bảo trì và nâng cấp

## Các thành phần chính

### 1. RedisConfig

Cấu hình Redis với:

- `RedisConnectionFactory`: Sử dụng Lettuce client
- `RedisTemplate<String, Object>`: Template với serialization:
  - Key: `StringRedisSerializer`
  - Value: `GenericJackson2JsonRedisSerializer` (JSON)

### 2. RedisService

Service cơ bản cho các thao tác Redis:

- `set(key, value)`: Lưu key-value
- `set(key, value, timeout, unit)`: Lưu với thời gian hết hạn
- `get(key)`: Lấy giá trị theo key
- `delete(key)`: Xóa key
- `hasKey(key)`: Kiểm tra key tồn tại
- `expire(key, timeout, unit)`: Đặt thời gian hết hạn
- `getExpire(key)`: Lấy thời gian còn lại
- `increment(key)`, `decrement(key)`: Tăng/giảm giá trị số

### 3. OtpService

Service quản lý OTP (One-Time Password):

- `generateOtp(email)`: Tạo OTP 6 số ngẫu nhiên
- `saveOtp(email, otp)`: Lưu OTP với thời gian hết hạn 3 phút
- `validateOtp(email, otp)`: Xác thực OTP và tự động xóa sau khi thành công
- `otpExists(email)`: Kiểm tra OTP có tồn tại
- `deleteOtp(email)`: Xóa OTP
- `getOtpExpireTime(email)`: Lấy thời gian còn lại của OTP

## Sử dụng

### 1. Thêm dependency vào pom.xml của service

```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>redis-common</artifactId>
</dependency>
```

### 2. Import configuration trong service của bạn

```java
@Configuration
@Import(com.example.rediscommon.config.RedisConfig.class)
public class YourServiceConfig {
    // Your configurations
}
```

Hoặc sử dụng component scan:

```java
@SpringBootApplication
@ComponentScan(basePackages = {"com.example.yourservice", "com.example.rediscommon"})
public class YourServiceApplication {
    // ...
}
```

### 3. Inject và sử dụng các service

#### Sử dụng RedisService

```java
@Service
@RequiredArgsConstructor
public class CacheService {
    private final RedisService redisService;

    public void cacheUserData(String userId, UserData data) {
        redisService.set("user:" + userId, data, 1, TimeUnit.HOURS);
    }

    public UserData getUserData(String userId) {
        return (UserData) redisService.get("user:" + userId);
    }
}
```

#### Sử dụng OtpService

```java
@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final OtpService otpService;

    public void sendOtpEmail(String email) {
        String otp = otpService.generateOtp(email);
        // Send OTP via email
        emailService.sendOtp(email, otp);
    }

    public boolean verifyOtp(String email, String otp) {
        return otpService.validateOtp(email, otp);
    }
}
```

## Cấu hình Redis

Thêm cấu hình Redis trong `application.properties` hoặc `application.yml`:

```properties
# Redis Configuration
spring.data.redis.host=localhost
spring.data.redis.port=6379
spring.data.redis.password=
spring.data.redis.database=0
spring.data.redis.timeout=60000
spring.data.redis.lettuce.pool.max-active=8
spring.data.redis.lettuce.pool.max-idle=8
spring.data.redis.lettuce.pool.min-idle=0
```

## Services đang sử dụng

- **auth-service**: Sử dụng OtpService cho xác thực email
- **stats-service**: Sẵn sàng sử dụng RedisService cho caching

## Lợi ích

1. **Tái sử dụng code**: Các service khác có thể sử dụng chung cấu hình và logic Redis
2. **Dễ bảo trì**: Chỉ cần cập nhật ở một nơi
3. **Nhất quán**: Đảm bảo tất cả service sử dụng cùng cấu hình Redis
4. **Mở rộng dễ dàng**: Có thể thêm các service Redis mới khi cần

## Phát triển tiếp theo

Có thể mở rộng module với:

- Session management service
- Cache service với TTL tự động
- Distributed lock service
- Rate limiting service
- Pub/Sub messaging service
