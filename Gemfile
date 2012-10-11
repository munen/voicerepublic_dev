source 'https://rubygems.org'

gem 'rails', '3.2.8'
gem 'rails-i18n'
gem 'pg'

# Gems used only for assets and not required
# in production environments by default.
group :assets do
  gem 'sass-rails',   '~> 3.2.3'
  gem 'coffee-rails', '~> 3.2.1'
  gem 'therubyracer', :platforms => :ruby
  gem 'uglifier', '>= 1.0.3'
end

gem 'twitter-bootstrap-rails'
gem 'jquery-rails'
gem 'devise'
gem 'omniauth-facebook'
gem 'omniauth-google-oauth2'
gem 'friendly_id'             # make urls more friendly
gem 'will_paginate'           # pagination-extension to active-record
gem 'will_paginate-bootstrap' # integrate twitter-bootstrap with will_paginate
gem 'i18n_data'               # delivers languages as key-value hash
gem 'paperclip'               # used for images
gem 'awesome_nested_set'
gem 'globalize3'
gem 'acts-as-taggable-on'
gem 'money'
gem 'cancan'                  # authorization/privileges
gem 'private_pub'             # push service
gem 'thin'                    # faster develpment-server
gem 'capistrano'              # deployment
gem 'thinking-sphinx'         # indexed search

# make rspec and cucumber the preferred test-suites
group :development, :test, :staging do
  gem 'rspec-rails'
  gem 'factory_girl_rails'
  gem 'faker'
end

group :test do
  gem 'faker'
  gem 'capybara'
  gem 'guard-rspec'
  gem 'launchy'
  gem 'database_cleaner'
  gem 'rb-inotify'
end


