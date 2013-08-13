Welcome to Kluuu
================

Setup
-----

### Thinking sphinx

    rake ts:config


Start the whole stack
---------------------

Rails, Faye (PrivatePub) & Sphinx.

    foreman start
    

Run Specs
---------

To run specs Faye and a Sphinx daemon have to run.


Branches and Hosts
------------------

develop

master     -> staging
production -> live


TODO
----

* rm app/models/rating.rb
* rm app/models/klu_image.rb
* Venue.update_all('user_id = 1', :user_id => nil)
* landing_page/index en/de -> localize
