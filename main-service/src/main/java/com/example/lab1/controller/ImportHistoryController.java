@RestController
@RequestMapping("/api/import-history")
@RequiredArgsConstructor
public class ImportHistoryController {
    private final ImportHistoryRepository importHistoryRepository;

    @PostMapping("/update")
    public ResponseEntity<Void> updateImportStatus(@RequestBody ImportHistoryUpdate update) {
        ImportHistory history = importHistoryRepository.findById(update.getImportHistoryId())
                .orElseThrow(() -> new RuntimeException("История импорта не найдена"));
        
        history.setStatus(update.isStatus());
        history.setCountElement(update.getCountElement());
        importHistoryRepository.save(history);
        
        return ResponseEntity.ok().build();
    }
}