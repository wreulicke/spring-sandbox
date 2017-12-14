package com.github.wreulicke.spring.transaction;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.Data;

@Data
@Entity
@Table
public class User {
	
	@Id
	@GeneratedValue
	@Column
	Long id;
	
	@Column
	String name;
	
}
