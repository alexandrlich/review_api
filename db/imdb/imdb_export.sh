#!/usr/bin/env ruby

require 'nokogiri'
require 'open-uri'
require 'csv'

# Fetch and parse HTML document
CSV.open("profile_data_imdb", "wb") do |csv|

	csv << ["id", "first_name", "last_name", "occupation", "skill", "year"]
	for i in 180..50000
		pageNumber = (i - 1) * 50 + 1
		url = "http://www.imdb.com/search/name?gender=male,female&ref_=nv_tp_cel_1&start=#{pageNumber}"
		begin
			doc = Nokogiri::HTML(open(url, :read_timeout=>60))
		rescue Exception => e
			puts e
			puts "Skipping page " + url
			sleep(300.0)
			next
		end
		doc.xpath('//table[@class="results"]//tr').each do |tr|
			number = tr.css('td[class=number]').text.gsub(/[^\d]/, '')
			nameTD = tr.css('td[class=name]')
			name = nameTD.css('a[href^="/name/nm"]')
			if !name.empty?
                
                id = name.first["href"]
				name = name.first.text
				ridx = name.rindex(" ")
				if !ridx.nil?
					name[ridx, 0] = ","
				else
					name += ","
				end
				name = name.split(",").map(&:strip)
				descAr = nameTD.css('span[class=description]').text.split(",").map(&:strip)
				csv << [number, name[0], name[1], descAr[0], descAr[1], ""]
                sleep(3.0)
                act_url = "http://imdb.com/#{id}"
                begin
                	act_doc = Nokogiri::HTML(open(act_url))
                rescue Exception => e
                	puts e
                	puts "Skipping person " + act_url
                	sleep(300.0)
                	next
                end

                act_doc.xpath('//*[@id="filmography"]/div[contains(@class, "filmo-category-section")]').each do |d0|
					cat_name = d0.previous_element.xpath('a').text
                	d0.xpath('div').each do |div|
                    	csv << ["", name[0], name[1] , cat_name,  div.xpath('b//a["href"]').text, div.xpath('span').text]
                    end
                end
			end
		end
	end
end
