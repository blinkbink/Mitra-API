package id.co.keriss.consolidate.ee;
// Generated 30 Nov 11 0:33:41 by Hibernate Tools 3.2.2.GA

/**
 * Transaction generated by hbm2java
 */
public class FeatureVO implements java.io.Serializable {

     private Long id;
     private String feature_name;
     private String feature_code;
     private String productbiller;
     private int nominal;



	public int getNominal() {
		return nominal;
	}

	public void setNominal(int nominal) {
		this.nominal = nominal;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFeature_name() {
		return feature_name;
	}

	public void setFeature_name(String feature_name) {
		this.feature_name = feature_name;
	}

	public String getFeature_code() {
		return feature_code;
	}

	public void setFeature_code(String feature_code) {
		this.feature_code = feature_code;
	}

    public String getProductbiller() {
		return productbiller;
	}

	public void setProductbiller(String productbiller) {
		this.productbiller = productbiller;
	}
	
	

    
    


}


