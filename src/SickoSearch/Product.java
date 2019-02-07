
package SickoSearch;

/**
 * Holds product information
 * @author muisl6048
 */
class Product {

    private String link;
    private String cost;
    private String name;
    
    /**
     * instantiates product
     * @param link
     * @param cost
     * @param name 
     */
    public Product(String link, String cost, String name) {
        
        this.link = link;
        this.cost = cost;
        this.name = name;
    }

    /**
     * @return the link
     */
    public String getLink() {
        return link;
    }

    /**
     * @param link the link to set
     */
    public void setLink(String link) {
        this.link = link;
    }

    /**
     * @return the cost
     */
    public String getCost() {
        return cost;
    }

    /**
     * @param cost the cost to set
     */
    public void setCost(String cost) {
        this.cost = cost;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * outputs all info
     * @return 
     */
    @Override
    public String toString() {
        return "Product{" + "link=" + link + ", cost=" + cost + ", name=" + name + '}';
    }
}
