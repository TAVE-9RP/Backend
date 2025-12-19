package com.nexerp.domain.item.service;

import com.nexerp.domain.item.model.request.ItemCreateRequest;
import com.nexerp.domain.item.model.response.ItemCreateResponse;
import com.nexerp.domain.item.model.entity.Item;
import com.nexerp.domain.item.repository.ItemRepository;
import com.nexerp.global.common.exception.BaseException;
import com.nexerp.global.common.exception.GlobalErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ItemService {

  private final ItemRepository itemRepository;
  @Transactional
  public ItemCreateResponse createItem(ItemCreateRequest request) {


    if (itemRepository.existsByCode(request.getCode())) {
      throw new BaseException(GlobalErrorCode.CONFLICT, "이미 존재하는 재고 번호입니다.");
    }

    Item item = Item.builder()
      .code(request.getCode())
      .name(request.getName())
      .price(request.getPrice())
      .quantity(0L)
      .location(request.getLocation())
      .createdAt(LocalDateTime.now())
      .receivedAt(null)
      .safetyStock(null)
      .targetStock(null)
      .build();

    Item saved = itemRepository.save(item);

    return ItemCreateResponse.from(saved.getId());
  }
}
