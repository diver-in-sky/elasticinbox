/**
 * Autogenerated by Avro
 * 
 * DO NOT EDIT DIRECTLY
 */
package com.elasticinbox.pipe.avro;  
@SuppressWarnings("all")
public class AvroAddress extends org.apache.avro.specific.SpecificRecordBase implements org.apache.avro.specific.SpecificRecord {
  public static final org.apache.avro.Schema SCHEMA$ = new org.apache.avro.Schema.Parser().parse("{\"type\":\"record\",\"name\":\"AvroAddress\",\"namespace\":\"com.elasticinbox.pipe.avro\",\"fields\":[{\"name\":\"name\",\"type\":\"string\",\"default\":\"\"},{\"name\":\"address\",\"type\":\"string\"}]}");
  @Deprecated public java.lang.CharSequence name;
  @Deprecated public java.lang.CharSequence address;
  public org.apache.avro.Schema getSchema() { return SCHEMA$; }
  // Used by DatumWriter.  Applications should not call. 
  public java.lang.Object get(int field$) {
    switch (field$) {
    case 0: return name;
    case 1: return address;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }
  // Used by DatumReader.  Applications should not call. 
  @SuppressWarnings(value="unchecked")
  public void put(int field$, java.lang.Object value$) {
    switch (field$) {
    case 0: name = (java.lang.CharSequence)value$; break;
    case 1: address = (java.lang.CharSequence)value$; break;
    default: throw new org.apache.avro.AvroRuntimeException("Bad index");
    }
  }

  /**
   * Gets the value of the 'name' field.
   */
  public java.lang.CharSequence getName() {
    return name;
  }

  /**
   * Sets the value of the 'name' field.
   * @param value the value to set.
   */
  public void setName(java.lang.CharSequence value) {
    this.name = value;
  }

  /**
   * Gets the value of the 'address' field.
   */
  public java.lang.CharSequence getAddress() {
    return address;
  }

  /**
   * Sets the value of the 'address' field.
   * @param value the value to set.
   */
  public void setAddress(java.lang.CharSequence value) {
    this.address = value;
  }

  /** Creates a new AvroAddress RecordBuilder */
  public static com.elasticinbox.pipe.avro.AvroAddress.Builder newBuilder() {
    return new com.elasticinbox.pipe.avro.AvroAddress.Builder();
  }
  
  /** Creates a new AvroAddress RecordBuilder by copying an existing Builder */
  public static com.elasticinbox.pipe.avro.AvroAddress.Builder newBuilder(com.elasticinbox.pipe.avro.AvroAddress.Builder other) {
    return new com.elasticinbox.pipe.avro.AvroAddress.Builder(other);
  }
  
  /** Creates a new AvroAddress RecordBuilder by copying an existing AvroAddress instance */
  public static com.elasticinbox.pipe.avro.AvroAddress.Builder newBuilder(com.elasticinbox.pipe.avro.AvroAddress other) {
    return new com.elasticinbox.pipe.avro.AvroAddress.Builder(other);
  }
  
  /**
   * RecordBuilder for AvroAddress instances.
   */
  public static class Builder extends org.apache.avro.specific.SpecificRecordBuilderBase<AvroAddress>
    implements org.apache.avro.data.RecordBuilder<AvroAddress> {

    private java.lang.CharSequence name;
    private java.lang.CharSequence address;

    /** Creates a new Builder */
    private Builder() {
      super(com.elasticinbox.pipe.avro.AvroAddress.SCHEMA$);
    }
    
    /** Creates a Builder by copying an existing Builder */
    private Builder(com.elasticinbox.pipe.avro.AvroAddress.Builder other) {
      super(other);
    }
    
    /** Creates a Builder by copying an existing AvroAddress instance */
    private Builder(com.elasticinbox.pipe.avro.AvroAddress other) {
            super(com.elasticinbox.pipe.avro.AvroAddress.SCHEMA$);
      if (isValidValue(fields()[0], other.name)) {
        this.name = (java.lang.CharSequence) data().deepCopy(fields()[0].schema(), other.name);
        fieldSetFlags()[0] = true;
      }
      if (isValidValue(fields()[1], other.address)) {
        this.address = (java.lang.CharSequence) data().deepCopy(fields()[1].schema(), other.address);
        fieldSetFlags()[1] = true;
      }
    }

    /** Gets the value of the 'name' field */
    public java.lang.CharSequence getName() {
      return name;
    }
    
    /** Sets the value of the 'name' field */
    public com.elasticinbox.pipe.avro.AvroAddress.Builder setName(java.lang.CharSequence value) {
      validate(fields()[0], value);
      this.name = value;
      fieldSetFlags()[0] = true;
      return this; 
    }
    
    /** Checks whether the 'name' field has been set */
    public boolean hasName() {
      return fieldSetFlags()[0];
    }
    
    /** Clears the value of the 'name' field */
    public com.elasticinbox.pipe.avro.AvroAddress.Builder clearName() {
      name = null;
      fieldSetFlags()[0] = false;
      return this;
    }

    /** Gets the value of the 'address' field */
    public java.lang.CharSequence getAddress() {
      return address;
    }
    
    /** Sets the value of the 'address' field */
    public com.elasticinbox.pipe.avro.AvroAddress.Builder setAddress(java.lang.CharSequence value) {
      validate(fields()[1], value);
      this.address = value;
      fieldSetFlags()[1] = true;
      return this; 
    }
    
    /** Checks whether the 'address' field has been set */
    public boolean hasAddress() {
      return fieldSetFlags()[1];
    }
    
    /** Clears the value of the 'address' field */
    public com.elasticinbox.pipe.avro.AvroAddress.Builder clearAddress() {
      address = null;
      fieldSetFlags()[1] = false;
      return this;
    }

    @Override
    public AvroAddress build() {
      try {
        AvroAddress record = new AvroAddress();
        record.name = fieldSetFlags()[0] ? this.name : (java.lang.CharSequence) defaultValue(fields()[0]);
        record.address = fieldSetFlags()[1] ? this.address : (java.lang.CharSequence) defaultValue(fields()[1]);
        return record;
      } catch (Exception e) {
        throw new org.apache.avro.AvroRuntimeException(e);
      }
    }
  }
}