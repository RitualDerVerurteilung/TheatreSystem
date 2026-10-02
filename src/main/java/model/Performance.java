package model;

public class Performance {
    int id;
    String title;
    String description;
    String date;
    int duration;
    double price;


    public void setId(int id) { this.id = id; }
    public int getId() {
        return id;
    }

    public void setTitle(String title) { this.title = title; }
    public String getTitle() {
        return title;
    }

    public void setDescription(String description) { this.description = description; }
    public String getDescription() {
        return description;
    }

    public void setDate(String date) { this.date = date; }
    public String getDate() {
        return date;
    }

    public void setDuration(int duration) { this.duration = duration; }
    public int getDuration() {
        return duration;
    }

    public void setPrice(double price) {
        this.price = price;
    }
    public double getPrice() {
        return price;
    }
}
