package com.nexerp.domain.item.service;

import com.nexerp.domain.item.model.request.ItemCreateRequest;
import com.nexerp.domain.item.model.response.ItemCreateResponse;
import com.nexerp.domain.item.model.entity.Item;
import com.nexerp.domain.item.model.response.ItemSearchResponse;
import com.nexerp.domain.item.repository.ItemRepository;
import com.nexerp.domain.member.model.entity.Member;
import com.nexerp.domain.member.repository.MemberRepository;
import com.nexerp.global.common.exception.BaseException;
import com.nexerp.global.common.exception.GlobalErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService {

  private final ItemRepository itemRepository;
  private final MemberRepository memberRepository;

  @Transactional
  public ItemCreateResponse createItem(Long memberId, ItemCreateRequest request) {

    Member member = memberRepository.findById(memberId)
      .orElseThrow(() -> new BaseException(GlobalErrorCode.NOT_FOUND, "직원 정보를 찾을 수 없습니다."));

    if (itemRepository.existsByCode(request.getCode())) {
      throw new BaseException(GlobalErrorCode.CONFLICT, "이미 존재하는 재고 번호입니다.");
    }

    Long companyId = member.getCompanyId();

    Item item = Item.builder()
      .companyId(companyId)
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

  @Transactional(readOnly = true)
  public List<ItemSearchResponse> searchItems(Long memberId, String keyword) {

    Member member = memberRepository.findById(memberId)
      .orElseThrow(() -> new BaseException(GlobalErrorCode.NOT_FOUND, "회원 정보를 찾을 수 없습니다."));

    Long companyId = member.getCompanyId();

    // 키워드 없는 경우는 전체 검색
    List<Item> items;

    if (keyword == null || keyword.isBlank()) {
      items = itemRepository.findAll();
    } else {
      items = itemRepository.searchByKeywordAndCompanyId(keyword, companyId);
    }

    return items.stream()
      .map(i -> ItemSearchResponse.builder()
        .itemId(i.getId())
        .code(i.getCode())
        .name(i.getName())
        .quantity(i.getQuantity())
        .location(i.getLocation())
        .price(i.getPrice())
        .build())
      .toList();
  }
}
