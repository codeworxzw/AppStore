package com.ricardotrujillo.appstore.model;

import java.util.ArrayList;

public final class Store {

    public final Feed feed;

    public Store(Feed feed) {
        this.feed = feed;
    }

    public static final class Feed {
        public final Author author;
        public final Updated updated;
        public final Rights rights;
        public final Title title;
        public final Icon icon;
        public final Link link[];
        public final Id id;
        public ArrayList<Entry> originalEntry = new ArrayList<>();
        public ArrayList<Entry> entry = new ArrayList<>();

        public Feed(Author author, Entry[] entries, Updated updated, Rights rights, Title title, Icon icon, Link[] link, Id id) {

            this.author = author;
            this.updated = updated;
            this.rights = rights;
            this.title = title;
            this.icon = icon;
            this.link = link;
            this.id = id;
        }

        public void fillOriginalEntry(ArrayList<Entry> entries) {

            if (this.originalEntry == null) this.originalEntry = new ArrayList<>();

            this.originalEntry.addAll(entries);
        }

        public static final class Author {
            public final Name name;
            public final Uri uri;

            public Author(Name name, Uri uri) {
                this.name = name;
                this.uri = uri;
            }

            public static final class Name {
                public final String label;

                public Name(String label) {
                    this.label = label;
                }
            }

            public static final class Uri {
                public final String label;

                public Uri(String label) {
                    this.label = label;
                }
            }
        }

        public static final class Entry {

            public final name name;
            public final image image[];
            public final Summary summary;
            public final price price;
            public final contentType contentType;
            public final Rights rights;
            public final Title title;
            public final Link link;
            public final Id id;
            public final artist artist;
            public final Category category;
            public final releaseDate releaseDate;
            public boolean imageLoaded = false;
            public int likes = 0;
            public boolean isLiked = false;

            public Entry(name name, image[] image, Summary summary, price price, contentType contentType, Rights rights, Title title, Link link, Id id, artist artist, Category category, releaseDate releaseDate) {
                this.name = name;
                this.image = image;
                this.summary = summary;
                this.price = price;
                this.contentType = contentType;
                this.rights = rights;
                this.title = title;
                this.link = link;
                this.id = id;
                this.artist = artist;
                this.category = category;
                this.releaseDate = releaseDate;
            }

            public static final class name {

                public String entryLabel;

                public String label;

                public name(String label) {

                    this.label = label;
                }
            }

            public static final class image {
                public final String label;
                public final Attributes attributes;

                public image(String label, Attributes attributes) {
                    this.label = label;
                    this.attributes = attributes;
                }

                public static final class Attributes {
                    public final String height;

                    public Attributes(String height) {
                        this.height = height;
                    }
                }
            }

            public static final class Summary {

                public String label;

                public Summary(String label) {
                    this.label = label;
                }
            }

            public static final class price {
                public final String label;
                public final Attributes attributes;

                public price(String label, Attributes attributes) {
                    this.label = label;
                    this.attributes = attributes;
                }

                public static final class Attributes {
                    public final double amount;
                    public final String currency;

                    public Attributes(String amount, String currency) {
                        this.amount = Double.valueOf(amount);
                        this.currency = currency;
                    }
                }
            }

            public static final class contentType {
                public final Attributes attributes;

                public contentType(Attributes attributes) {
                    this.attributes = attributes;
                }

                public static final class Attributes {
                    public final String term;
                    public final String label;

                    public Attributes(String term, String label) {
                        this.term = term;
                        this.label = label;
                    }
                }
            }

            public static final class Rights {
                public final String label;

                public Rights(String label) {
                    this.label = label;
                }
            }

            public static final class Title {
                public final String label;

                public Title(String label) {
                    this.label = label;
                }
            }

            public static final class Link {
                public final Attributes attributes;

                public Link(Attributes attributes) {
                    this.attributes = attributes;
                }

                public static final class Attributes {
                    public final String rel;
                    public final String type;
                    public final String href;

                    public Attributes(String rel, String type, String href) {
                        this.rel = rel;
                        this.type = type;
                        this.href = href;
                    }
                }
            }

            public static final class Id {
                public final String label;
                public final Attributes attributes;

                public Id(String label, Attributes attributes) {
                    this.label = label;
                    this.attributes = attributes;
                }

                public static final class Attributes {
                    public final String id;
                    public final String bundleId;

                    public Attributes(String id, String bundleId) {
                        this.id = id;
                        this.bundleId = bundleId;
                    }
                }
            }

            public static final class artist {
                public final String label;
                public final Attributes attributes;

                public artist(String label, Attributes attributes) {
                    this.label = label;
                    this.attributes = attributes;
                }

                public static final class Attributes {
                    public final String href;

                    public Attributes(String href) {
                        this.href = href;
                    }
                }
            }

            public static final class Category {
                public final Attributes attributes;

                public Category(Attributes attributes) {
                    this.attributes = attributes;
                }

                public static final class Attributes {
                    public final String id;
                    public final String term;
                    public final String scheme;
                    public final String label;

                    public Attributes(String id, String term, String scheme, String label) {
                        this.id = id;
                        this.term = term;
                        this.scheme = scheme;
                        this.label = label;
                    }
                }
            }

            public static final class releaseDate {
                public final String label;
                public final Attributes attributes;

                public releaseDate(String label, Attributes attributes) {
                    this.label = label;
                    this.attributes = attributes;
                }

                public static final class Attributes {
                    public final String label;

                    public Attributes(String label) {
                        this.label = label;
                    }
                }
            }
        }

        public static final class Updated {
            public final String label;

            public Updated(String label) {
                this.label = label;
            }
        }

        public static final class Rights {
            public final String label;

            public Rights(String label) {
                this.label = label;
            }
        }

        public static final class Title {
            public final String label;

            public Title(String label) {
                this.label = label;
            }
        }

        public static final class Icon {
            public final String label;

            public Icon(String label) {
                this.label = label;
            }
        }

        public static final class Link {
            public final Attributes attributes;

            public Link(Attributes attributes) {
                this.attributes = attributes;
            }

            public static final class Attributes {
                public final String rel;
                public final String href;

                public Attributes(String rel, String href) {
                    this.rel = rel;
                    this.href = href;
                }
            }
        }

        public static final class Id {
            public final String label;

            public Id(String label) {
                this.label = label;
            }
        }
    }
}
