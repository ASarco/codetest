package se.kry.codetest;

import java.time.LocalDateTime;
import java.util.Objects;

public class Service {

    private final String url;
    private final String name;
    private final Status status;
    private final LocalDateTime addedOn;

    public Service(String url, String name, Status status, LocalDateTime addedOn) {
        this.url = url;
        this.name = name;
        this.status = status;
        this.addedOn = addedOn;
    }

    public String getUrl() {
        return url;
    }

    public Status getStatus() {
        return status;
    }

    public String getName() {
        return name;
    }

    public LocalDateTime getAddedOn() {
        return addedOn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Service service = (Service) o;
        return Objects.equals(url, service.url) &&
                Objects.equals(name, service.name) &&
                status == service.status &&
                Objects.equals(addedOn, service.addedOn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url, name, status, addedOn);
    }

    @Override
    public String toString() {
        return "Service{" +
                "url='" + url + '\'' +
                ", name='" + name + '\'' +
                ", status=" + status +
                ", addedOn=" + addedOn +
                '}';
    }
}
