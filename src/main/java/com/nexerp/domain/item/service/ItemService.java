package com.nexerp.domain.item.service;

import com.nexerp.domain.item.model.entity.Item;
import com.nexerp.domain.item.model.entity.ItemHistory;
import com.nexerp.domain.item.model.request.ItemCreateRequest;
import com.nexerp.domain.item.model.response.ItemCreateResponse;
import com.nexerp.domain.item.model.response.ItemDetailResponse;
import com.nexerp.domain.item.model.response.ItemHistoryResponse;
import com.nexerp.domain.item.model.response.ItemSearchResponse;
import com.nexerp.domain.item.repository.ItemHistoryRepository;
import com.nexerp.domain.item.repository.ItemRepository;
import com.nexerp.domain.member.model.entity.Member;
import com.nexerp.domain.member.repository.MemberRepository;
import com.nexerp.domain.member.service.MemberService;
import com.nexerp.global.common.exception.BaseException;
import com.nexerp.global.common.exception.GlobalErrorCode;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ItemService {

  private final ItemRepository itemRepository;
  private final MemberRepository memberRepository;
  private final MemberService memberService;
  private final ItemHistoryRepository itemHistoryRepository;

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
      .safetyStock(0L)
      .targetStock(0L)
      .build();

    Item saved = itemRepository.save(item);

    return ItemCreateResponse.from(saved.getId());
  }

  @Transactional(readOnly = true)
  public List<ItemSearchResponse> searchItems(Long memberId, String keyword) {

    Member member = memberRepository.findById(memberId)
      .orElseThrow(() -> new BaseException(GlobalErrorCode.NOT_FOUND, "회원 정보를 찾을 수 없습니다."));

    Long companyId = member.getCompanyId();

    List<Item> items;

    // 키워드 없으면 회사별 전체
    if (keyword == null || keyword.isBlank()) {
      items = itemRepository.findAllByCompanyId(companyId);
    } else {
      // 있으면 회사 + 키워드
      items = itemRepository.searchByKeywordAndCompanyId(keyword, companyId);
    }

    return items.stream()
      .map(ItemSearchResponse::from)
      .toList();
  }

  @Transactional(readOnly = true)
  public List<ItemHistoryResponse> getItemHistories(Long memberId, Long itemId) {

    Long memberCompanyId = memberService.getCompanyIdByMemberId(memberId);

    Item item = itemRepository.findByIdAndCompanyId(itemId, memberCompanyId)
      .orElseThrow(() -> new BaseException(GlobalErrorCode.FORBIDDEN, "회원 회사의 물품이 아닙니다."));

    List<ItemHistory> itemHistories = itemHistoryRepository.findByItemIdOrderByProcessedAtDesc(
      item.getId());

    return ItemHistoryResponse.fromList(itemHistories);
  }

  @Transactional(readOnly = true)
  public ItemDetailResponse getItemDetail(Long memberId, Long itemId) {
    Long memberCompanyId = memberService.getCompanyIdByMemberId(memberId);

    Item item = itemRepository.findByIdAndCompanyId(itemId, memberCompanyId)
      .orElseThrow(() -> new BaseException(GlobalErrorCode.FORBIDDEN, "회원 회사의 물품이 아닙니다."));
    return ItemDetailResponse.from(item);
  }

  @Transactional
  public void updateItemTargetStock(Long memberId, Long itemId,
    Long targetStock) {
    Long memberCompanyId = memberService.getCompanyIdByMemberId(memberId);

    Item item = itemRepository.findByIdAndCompanyId(itemId, memberCompanyId)
      .orElseThrow(() -> new BaseException(GlobalErrorCode.FORBIDDEN, "회원 회사의 물품이 아닙니다."));

    item.updateItemTargetStock(targetStock);
  }

  @Transactional
  public void updateItemSafetyStock(Long memberId, Long itemId, Long safetyStock) {
    Long memberCompanyId = memberService.getCompanyIdByMemberId(memberId);

    Item item = itemRepository.findByIdAndCompanyId(itemId, memberCompanyId)
      .orElseThrow(() -> new BaseException(GlobalErrorCode.FORBIDDEN, "회원 회사의 물품이 아닙니다."));

    item.updateItemSafetyStock(safetyStock);
  }
}
