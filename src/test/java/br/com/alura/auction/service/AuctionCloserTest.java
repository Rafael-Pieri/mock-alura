package br.com.alura.auction.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.alura.auction.builder.AuctionCreator;
import br.com.alura.auction.domain.Auction;
import br.com.alura.auction.repository.AuctionDao;
import br.com.alura.auction.repository.AuctionRepository;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import org.junit.Test;
import org.mockito.InOrder;

public class AuctionCloserTest {

    @Test
    public void shouldCloseAuctionsStartedALastWeek() {
        Calendar oldDate = Calendar.getInstance();
        oldDate.set(1999, 1, 20);

        Auction auctionOne = new AuctionCreator().to("Plasma TV")
            .onDate(oldDate)
            .build();

        Auction auctionTwo = new AuctionCreator().to("Refrigerator")
            .onDate(oldDate)
            .build();

        List<Auction> oldAuctions = Arrays.asList(auctionOne, auctionTwo);

        AuctionRepository fakeAuctionRepository = mock(AuctionRepository.class);

        when(fakeAuctionRepository.current()).thenReturn(oldAuctions);

        MailMan fakeMailMan = mock(MailMan.class);

        AuctionCloser auctionCloser = new AuctionCloser(fakeAuctionRepository, fakeMailMan);
        auctionCloser.close();

        assertEquals(2, auctionCloser.getTotalClosed());
        assertTrue(auctionOne.isClosed());
        assertTrue(auctionTwo.isClosed());

        verify(fakeAuctionRepository, times(1)).update(auctionOne);

        InOrder inOrder = inOrder(fakeAuctionRepository, fakeMailMan);

        inOrder.verify(fakeAuctionRepository, times(1)).update(auctionOne);
        inOrder.verify(fakeMailMan, times(1)).send(auctionOne);
    }

    @Test
    public void shouldNotCloseAuctionsStartedYesterday() {
        Calendar oldDate = Calendar.getInstance();
        oldDate.add(Calendar.DATE, -1);

        Auction auctionOne = new AuctionCreator().to("Plasma TV")
            .onDate(oldDate)
            .build();

        Auction auctionTwo = new AuctionCreator().to("Refrigerator")
            .onDate(oldDate)
            .build();

        List<Auction> oldAuctions = Arrays.asList(auctionOne, auctionTwo);

        AuctionRepository fakeAuctionRepository = mock(AuctionRepository.class);

        when(fakeAuctionRepository.current()).thenReturn(oldAuctions);

        MailMan fakeMailMan = mock(MailMan.class);

        AuctionCloser auctionCloser = new AuctionCloser(fakeAuctionRepository, fakeMailMan);
        auctionCloser.close();

        assertEquals(0, auctionCloser.getTotalClosed());
        assertFalse(auctionOne.isClosed());
        assertFalse(auctionTwo.isClosed());
    }

    @Test
    public void shouldNotCloseAuctionsWhenThereAreNoAuctionsInProcess() {
        AuctionDao fakeAuctionRepository = mock(AuctionDao.class);

        when(fakeAuctionRepository.current()).thenReturn(new ArrayList<Auction>());

        MailMan fakeMailMan = mock(MailMan.class);

        AuctionCloser auctionCloser = new AuctionCloser(fakeAuctionRepository, fakeMailMan);
        auctionCloser.close();

        assertEquals(0, auctionCloser.getTotalClosed());
    }

    @Test
    public void shouldNotCloseAuctionsStartedLessThanAWeekAgo() {

        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_MONTH, -1);

        Auction auctionOne = new AuctionCreator().to("Plasma TV")
            .onDate(yesterday)
            .build();

        Auction auctionTwo = new AuctionCreator().to("Refrigerator")
            .onDate(yesterday)
            .build();

        AuctionRepository fakeAuctionRepository = mock(AuctionDao.class);

        when(fakeAuctionRepository.current()).thenReturn(Arrays.asList(auctionOne, auctionTwo));

        MailMan fakeMailMan = mock(MailMan.class);

        AuctionCloser auctionCloser = new AuctionCloser(fakeAuctionRepository, fakeMailMan);
        auctionCloser.close();

        assertEquals(0, auctionCloser.getTotalClosed());
        assertFalse(auctionOne.isClosed());
        assertFalse(auctionTwo.isClosed());

        verify(fakeAuctionRepository, never()).update(auctionOne);
        verify(fakeAuctionRepository, never()).update(auctionTwo);
    }

    @Test
    public void shouldContinueExecutionEvenIfDaoFails() {
        Calendar oldDate = Calendar.getInstance();
        oldDate.set(1999, 1, 20);

        Auction auctionOne = new AuctionCreator().to("Plasma TV")
            .onDate(oldDate)
            .build();

        Auction auctionTwo = new AuctionCreator().to("Refrigerator")
            .onDate(oldDate)
            .build();

        AuctionRepository fakeAuctionRepository = mock(AuctionRepository.class);

        when(fakeAuctionRepository.current()).thenReturn(Arrays.asList(auctionOne, auctionTwo));

        doThrow(new RuntimeException()).when(fakeAuctionRepository).update(auctionOne);

        MailMan fakeMailMan = mock(MailMan.class);

        doThrow(new RuntimeException()).when(fakeMailMan).send(auctionOne);

        AuctionCloser auctionCloser = new AuctionCloser(fakeAuctionRepository, fakeMailMan);
        auctionCloser.close();

        verify(fakeAuctionRepository).update(auctionTwo);
        verify(fakeMailMan).send(auctionTwo);
    }

    @Test
    public void shouldThrowExceptionToAllAuctionsOnTheList() {
        Calendar oldDate = Calendar.getInstance();
        oldDate.set(1999, 1, 20);

        Auction auctionOne = new AuctionCreator().to("Plasma TV")
            .onDate(oldDate)
            .build();

        Auction auctionTwo = new AuctionCreator().to("Refrigerator")
            .onDate(oldDate)
            .build();

        AuctionRepository fakeAuctionRepository = mock(AuctionRepository.class);

        when(fakeAuctionRepository.current()).thenReturn(Arrays.asList(auctionOne, auctionTwo));

        MailMan fakeMailMan = mock(MailMan.class);

        doThrow(new RuntimeException()).when(fakeAuctionRepository).update(auctionOne);
        doThrow(new RuntimeException()).when(fakeAuctionRepository).update(auctionTwo);

        AuctionCloser auctionCloser = new AuctionCloser(fakeAuctionRepository, fakeMailMan);
        auctionCloser.close();

        verify(fakeMailMan, never()).send(auctionOne);
        verify(fakeMailMan, never()).send(auctionTwo);
    }

    @Test
    public void shouldThrowExceptionToAllAuctionsOnTheListUsingAny() {
        Calendar oldDate = Calendar.getInstance();
        oldDate.set(1999, 1, 20);

        Auction auctionOne = new AuctionCreator().to("Plasma TV")
            .onDate(oldDate)
            .build();

        Auction auctionTwo = new AuctionCreator().to("Refrigerator")
            .onDate(oldDate)
            .build();

        AuctionRepository fakeAuctionRepository = mock(AuctionRepository.class);

        when(fakeAuctionRepository.current()).thenReturn(Arrays.asList(auctionOne, auctionTwo));

        MailMan fakeMailMan = mock(MailMan.class);

        doThrow(new RuntimeException()).when(fakeAuctionRepository).update(any(Auction.class));

        AuctionCloser auctionCloser = new AuctionCloser(fakeAuctionRepository, fakeMailMan);
        auctionCloser.close();

        verify(fakeMailMan, never()).send(any(Auction.class));
    }
}