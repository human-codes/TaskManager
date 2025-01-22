package tmanager.service;

import org.springframework.stereotype.Service;
import tmanager.entity.Card;

import java.util.List;
import java.util.Comparator;
import java.util.stream.Collectors;

@Service
public class CardService {

    public List<Card> sortCards(List<Card> cards) {
        return cards.stream()
                .sorted(Comparator
                        .comparing(Card::isStarter).reversed() // Starter cards first
                        .thenComparing(Card::isFinisher))     // Finisher cards last
                .collect(Collectors.toList());
    }
}



