package minutes.tracker.app.dto;

import java.util.List;

/**
 *
 * JSON serializable DTO containing data concerning a work search request.
 *
 */
public class WorksDTO {

    private long currentPage;
    private long totalPages;
    List<WorkDTO> works;

    public WorksDTO(long currentPage, long totalPages, List<WorkDTO> works) {
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.works = works;
    }

    public long getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public long getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public List<WorkDTO> getWorks() {
        return works;
    }

    public void setWorks(List<WorkDTO> works) {
        this.works = works;
    }
}
