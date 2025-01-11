@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImportMessage implements Serializable {
    private String fileName;
    private Long importHistoryId;
    private Long userId;
    private String contentType;
}