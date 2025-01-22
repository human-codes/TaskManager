package tmanager.service;

import org.springframework.stereotype.Service;
import tmanager.entity.Card;

import java.util.ArrayList;
import java.util.List;
import java.util.Comparator;
import java.util.stream.Collectors;

@Service
public class CardService {

//    public List<Card> sortCards(List<Card> cards) {
//        return cards.stream()
//                .sorted(Comparator
//                        .comparing(Card::isStarter).reversed() // Starter cards first
//                        .thenComparing(Card::isFinisher))     // Finisher cards last
//                .collect(Collectors.toList());
//    }


    public List<Card> sortAndAssignOrders(List<Card> cards) {
        // Separate cards into starter, finisher, and other cards
        Card starterCard = cards.stream().filter(Card::isStarter).findFirst().orElse(null);
        Card finisherCard = cards.stream().filter(Card::isFinisher).findFirst().orElse(null);

        // Filter out starter and finisher cards
        List<Card> middleCards = cards.stream()
                .filter(card -> !card.isStarter() && !card.isFinisher())
                .sorted(Comparator.comparing(Card::getOrders))
                .collect(Collectors.toList());

        // Assign order numbers to middle cards
        int order = 1;
        for (Card card : middleCards) {
            card.setOrders(order++);
        }

        // Combine all cards
        List<Card> sortedCards = new ArrayList<>();
        if (starterCard != null) {
            sortedCards.add(starterCard);
        }
        sortedCards.addAll(middleCards);
        if (finisherCard != null) {
            sortedCards.add(finisherCard);
        }

        return sortedCards;
    }

}



