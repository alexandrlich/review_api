#!/usr/bin/env ruby

require 'nokogiri'
require 'open-uri'
require 'json'

# Fetch and parse HTML document
("a".."z").each { |sl|
    File.open("profile_data_last_fm_#{sl}", "wb") do |f|
        url_fp = "http://www.last.fm/search/overview?q=#{sl}&type=artist"
        begin
            fp = Nokogiri::HTML(open(url_fp, :read_timeout=>60))
        rescue Exception => error
            puts error
            puts "Skipping page " + url_fp
            sleep(300.0)
            next
        end

        pages = fp.xpath('//*[@id="content"]/div[3]/div/div[3]/a[2]').text
        f.write("[")
	    for pageNumber in 1..pages.to_i
		    url = "http://www.last.fm/search/overview?q=#{sl}&type=artist&page=#{pageNumber}"
		    begin
		        doc = Nokogiri::HTML(open(url, :read_timeout=>60))
		    rescue Exception => err
		        puts err
		        puts ("Skipped artist url: " + url)
		        sleep(300.0)
		        next
		    end

		    doc.xpath('//ul[@class="artistsWithInfo"]//li').each do |li|
			    name = li.xpath('a/strong').text
                a = li.xpath('a').first
			    last_fm_id = a["href"]
			    albums_url ="http://www.last.fm#{last_fm_id}/+albums"
                sleep(1.0)
                begin
                    albums_doc = Nokogiri::HTML(open(albums_url, :read_timeout=>60))
                rescue Exception => er
                    puts er
                    puts ("Skipped albums url: " + albums_url)
                    sleep(300.0)
                    next
                end
                array = []
                albums_doc.xpath('//ul/li/section/div/div[1]').each do |ali|
                    title = ali.xpath('a/h3').text

                    dtc = ali.xpath('time')
                    if !dtc.empty?
                        dtc = dtc.first["datetime"]
                    end

                    al = {
                        "al" => title,
                        "dt" => dtc
                    }
                    array << al
                end
                sleep(1.0)
                main_page = Nokogiri::HTML(open("http://www.last.fm#{last_fm_id}", :read_timeout=>60))

                tags = []
                main_page.xpath('/html/body/div[2]/article/div[2]/div[1]/section[1]/section/ul/li/a').each do |tag|

                    tags << tag.text
                end

                scrobblers = main_page.xpath('/html/body/div[2]/article/div[1]/section[2]/div/div[2]/div/ul/li[1]/b')
                if !scrobblers.empty?
                    scrobblers = scrobblers.first["data-count"]
                end

                listeners = main_page.xpath('//*[@id="listenerCount"]')
                if !listeners.empty?
                    listeners = listeners.first["data-count"]
                end
            
			    j = {
			        "n" => name,
                    "albums" => array,
                    "tags" => tags,
                    "s" => scrobblers,
                    "l" => listeners
			    }


                f.write(j.to_json)
                f.write(",")

		    end
		    sleep(1.0)
	    end
	    f.write("]")
    end
}
