package br.com.alura.auction.domain;

import br.com.alura.auction.config.JPAConfiguration;
import br.com.alura.auction.repository.AuctionDao;
import br.com.alura.auction.repository.AuctionRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {JPAConfiguration.class, AuctionDao.class})
public class AuctionDaoTest {

    @Autowired
    private AuctionRepository auctionDao;

    @Test
    public void saveAuction() {
        Auction auction = new Auction("BMW 320");

        auctionDao.save(auction);
    }
}