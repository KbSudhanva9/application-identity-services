package com.auth.dto;


import java.util.List;

 
public record PaginationResponse(

   List  content,

      int page,

      int size,

      long totalElements,

     int totalPages,

     boolean last) {
    	 
     }
