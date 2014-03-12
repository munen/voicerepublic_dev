require 'spec_helper'

describe "Talks" do

  before do
    @user = FactoryGirl.create(:user)
    login_user(@user)
  end

  describe "validation" do

    before do
      @venue = FactoryGirl.create :venue
      @talk = FactoryGirl.create :talk, venue: @venue, tag_list: "test, foo, bar"
    end

    # FIXME sometimes failing specs (on dev as well as circle ci) when using
    # ANONYMOUS users.
    #
    #     Failure/Error: Unable to find matching line from backtrace
    #     ActiveRecord::RecordNotFound:
    #       ActiveRecord::RecordNotFound
    #
    it "can be shared to social networks and saves statistics", driver: :chrome, slow: true do
      SocialShare.count.should eq(0)
      visit venue_talk_path 'en', @venue, @talk
      page.execute_script('$("#social_share .facebook").click()')
      sleep 0.2

      share_window = page.driver.browser.window_handles.last
      page.within_window share_window do
       current_url.should match(/facebook.com/)
      end

      SocialShare.count.should eq(1)
    end

    # FIXME sometimes failing specs (on dev as well as circle ci) when using
    # ANONYMOUS users.
    #
    it "does not lose tags on failed validation", js: true do
      visit edit_venue_talk_path 'en', @venue, @talk
      fill_in :talk_title, with: ""
      click_on I18n.t 'helpers.submit.submit'
      page.should have_content "Please review the problems below"
      within '#s2id_talk_tag_list.tagList' do
        page.should have_content "test"
        page.should have_content "foo"
        page.should have_content "bar"
      end
    end
  end
end
