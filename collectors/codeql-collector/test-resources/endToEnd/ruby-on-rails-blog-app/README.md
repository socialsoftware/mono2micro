# Ruby on Rails Tutorial sample application

This is a fork of the [*Ruby on Rails Tutorial*](http://www.railstutorial.org/) application by Michael Hartl.
We created this project to help you try RubyMine features.
To get started with the app, follow the [step-by-step tutorial](https://www.jetbrains.com/help/ruby/get-started.html).

## License

All source code in the [Ruby on Rails Tutorial](https://www.railstutorial.org/)
is available jointly under the MIT License and the Beerware License. See
[LICENSE.md](LICENSE.md) for details.

## Getting started

To get started with the app, clone the repo and then install the needed gems. You can clone the repo as follows:

```
$ git clone https://github.com/Avrelina/sample_rails_app_8th_ed
$ cd sample_rails_app_8th_ed/
```

To install the gems, you will need the same versions of Ruby used to build the sample app, which you can find using the `cat` command as follows:

```
$ cat .ruby-version
<Ruby version number>
```

Once you configured `ruby`, install required gems using following command with `--without production` flag:
```
bundle install
```

If you run into any trouble, you can remove `Gemfile.lock` and rebundle at any time:

```
$ rm -f Gemfile.lock
```
and then with `--without production` flag
```
bundle install
```

Next, migrate the database:

```
rake db:migrate
```

Finally, run the test suite to verify that everything is working correctly:

```
rake test
```

If the test suite passes, you’ll be ready to seed the database with sample users and run the app in a local server:

```
rake db:seed
```
```
rails server
```
