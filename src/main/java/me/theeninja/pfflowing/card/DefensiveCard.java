package me.theeninja.pfflowing.card;

import me.theeninja.pfflowing.Side;
import me.theeninja.pfflowing.card.Author;
import me.theeninja.pfflowing.card.Card;
import me.theeninja.pfflowing.card.Content;
import me.theeninja.pfflowing.flowing.Defensive;
import me.theeninja.pfflowing.gui.FlowingColumnsController;
import me.theeninja.pfflowing.gui.PFFlowingApplicationController;

import java.util.Calendar;

public class DefensiveCard extends Card implements Defensive {
    public DefensiveCard(Author author, String source, Calendar date, Content cardContnet, Side initiator) {
        super(author, source, date, cardContnet, initiator);
    }
}
