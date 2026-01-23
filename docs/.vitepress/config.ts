import { defineConfig } from 'vitepress'

// https://vitepress.dev/reference/site-config
export default defineConfig({
  title: "Hytale Kotlin Library",
  description: "Kotlin extensions and DSLs for Hytale server plugin development",
  appearance: true,
  themeConfig: {
    // https://vitepress.dev/reference/default-theme-config
    nav: [
      { text: 'Home', link: '/' },
      { text: 'Guide', link: '/guide/getting-started' },
      { text: 'Reference', link: '/reference/overview' }
    ],

    sidebar: {
      '/guide/': [
        {
          text: 'Introduction',
          items: [
            { text: 'Getting Started', link: '/guide/getting-started' }
          ]
        },
        {
          text: 'Core Features',
          items: [
            { text: 'Commands', link: '/guide/commands' },
            { text: 'ECS (Entities)', link: '/guide/ecs' },
            { text: 'Events', link: '/guide/events' },
            { text: 'Interactions', link: '/guide/interactions' },
            { text: 'Items', link: '/guide/items' },
            { text: 'UI', link: '/guide/ui' }
          ]
        }
      ],
      '/reference/': [
        {
          text: 'API Reference',
          items: [
            { text: 'Overview', link: '/reference/overview' }
          ]
        }
      ]
    },

    socialLinks: [
      { icon: 'github', link: 'https://github.com/BetrixDev/hytale-kotlin-library' }
    ],

    search: {
      provider: 'local'
    },

    footer: {
      message: 'MIT License',
      copyright: `Copyright © ${new Date().getFullYear()} BetrixDev`
    }
  },

  head: [
    ['link', { rel: 'stylesheet', href: '/custom-theme.css' }]
  ]
})
