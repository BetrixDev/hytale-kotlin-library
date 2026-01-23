import { Glob } from "bun";
import { McpServer } from "@modelcontextprotocol/sdk/server/mcp.js";
import { BunSSEServerTransport } from "bun-mcp-sse-transport/src/index.js";
import { z } from "zod";
import matter from "gray-matter";
import { distance } from "fastest-levenshtein";

const markdownFilesGlob = new Glob("../**/*.md");

const docsIndex: {
  title: string;
  description: string;
  content: string;
}[] = [];

for await (const filePath of markdownFilesGlob.scan()) {
  if (filePath.includes("mcp") || filePath.includes("index.md")) {
    continue;
  }

  const file = Bun.file(filePath);

  const fileContent = matter(await file.text());

  if (!fileContent.data.title || !fileContent.data.description) {
    console.error(`Missing title or description in ${filePath}`);
    continue;
  }

  docsIndex.push({
    title: fileContent.data.title,
    description: fileContent.data.description,
    content: fileContent.content,
  });
}

const server = new McpServer({
  name: "Hytale Kotlin Library Documentation MCP Server",
  version: "1.0.0",
  description: "Documentation for the Hytale Kotlin Library",
  websiteUrl: "https://hytale-kotlin-library.vercel.app",
});

server.registerTool(
  "get_pages",
  { title: "Get Pages", description: "Get all documentation pages" },
  () => {
    const pages = docsIndex.map((page) => ({
      title: page.title ?? "Untitled",
      description: page.description ?? "No description",
    }));

    const formattedList = pages
      .map((p, i) => `${i + 1}. **${p.title}** - ${p.description}`)
      .join("\n");

    return {
      content: [
        {
          type: "text" as const,
          text: `# Available Documentation Pages\n\nUse the \`get_page\` tool with the exact page title to retrieve its content.\n\n${formattedList}\n\n---\n\nTotal: ${pages.length} pages`,
        },
      ],
    };
  }
);

server.registerTool(
  "get_page",
  {
    title: "Get Page",
    description: "Get a specific documentation page",
    inputSchema: z.object({
      pageTitle: z.string(),
    }),
  },
  ({ pageTitle }) => {
    const page = docsIndex.find((page) => page.title === pageTitle);

    if (!page) {
      const closestPage = docsIndex.sort(
        (a, b) =>
          distance(a.title.toLowerCase(), pageTitle.toLowerCase()) -
          distance(b.title.toLowerCase(), pageTitle.toLowerCase())
      )[0];

      return {
        content: [
          {
            type: "text",
            text: `Page not found. Did you mean ${
              closestPage?.title ?? "no such page"
            }?`,
          },
        ],
        isError: true,
      };
    }

    return {
      content: [
        {
          type: "text",
          text: page.content,
        },
      ],
    };
  }
);

const transports: Record<string, BunSSEServerTransport> = {};

Bun.serve({
  port: 3000,
  idleTimeout: 255,
  routes: {
    "/sse": () => {
      const transport = new BunSSEServerTransport("/messages");
      server.connect(transport);
      transport.onclose = () => {
        delete transports[transport.sessionId];
      };
      transports[transport.sessionId] = transport;
      return transport.createResponse();
    },
    "/messages": (req) => {
      const url = new URL(req.url);
      const sessionId = url.searchParams.get("sessionId");
      if (!sessionId || !transports[sessionId]) {
        return new Response("Invalid session ID", { status: 400 });
      }
      return transports[sessionId].handlePostMessage(req);
    },
    "/health": () => {
      return new Response("OK", { status: 200 });
    },
  },
  fetch() {
    return new Response("Not Found", { status: 404 });
  },
});

console.log("MCP server running at http://localhost:3000/sse");
