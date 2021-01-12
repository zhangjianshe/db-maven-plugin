import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class DbParam {
    private String url;
    private String user;
    private String password;
    private String daoPackage;
    private String entityPackage;
    private String schema;
    private String path;
}
