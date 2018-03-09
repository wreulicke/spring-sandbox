/**
 * MIT License
 *
 * Copyright (c) 2017 Wreulicke
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.github.wreulicke.simple.product;

import java.awt.*;
import java.util.Optional;

import javax.websocket.server.PathParam;

import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

  private final ProductRepository productRepository;

  private final ProductStockRepository productStockRepository;

  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
  public ProductResponse create(@RequestBody CreateProductRequest request) {
    Product product = new Product();
    product.setDescription(request.getDescription());

    Product savedProduct = productRepository.save(product);

    ProductStock stock = new ProductStock();
    stock.setId(savedProduct.getId());
    stock.setCount(request.getCount()
      .orElse(0L));

    ProductStock savedStock = productStockRepository.save(stock);

    ProductResponse productResponse = new ProductResponse(savedProduct, savedStock);
    return productResponse;
  }


  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  @GetMapping("/{productId}")
  public ResponseEntity<?> get(@PathParam("productId") Long productId) {

    Optional<Product> product = productRepository.findById(productId);

    if (!product.isPresent()) {
      return ResponseEntity.notFound()
        .build();
    }

    return product.map(ProductResponse::new)
      .map(ResponseEntity::ok)
      .get();
  }


  @PreAuthorize("hasAuthority('ROLE_ADMIN')")
  @PostMapping("/{productId}")
  public ResponseEntity<?> update(@PathParam("productId") Long productId, @RequestBody UpdateProductRequest request) {
    Optional<Product> product = productRepository.findById(productId);

    if (!product.isPresent()) {
      return ResponseEntity.notFound()
        .build();
    }

    Product p = product.get();
    request.getDescription()
      .ifPresent(p::setDescription);

    Optional<ProductStock> stock = productStockRepository.findById(productId);
    stock.ifPresent(s -> request.getCount()
      .ifPresent(s::setCount));

    return product.map(ProductResponse::new)
      .map(ResponseEntity::ok)
      .get();
  }


}
